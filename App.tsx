
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { DailyStats, ViewType, UserProfile } from './types';
import Dashboard from './components/Dashboard';
import HistoryView from './components/History';
import CalendarView from './components/CalendarView';
import Settings from './components/Settings';
import Navigation from './components/Navigation';

const STORAGE_KEY = 'zenstep_data';
const PROFILE_KEY = 'zenstep_profile';
const THEME_KEY = 'zenstep_theme';
const TRACKING_KEY = 'zenstep_tracking_state';

const App: React.FC = () => {
  const [activeView, setActiveView] = useState<ViewType>('dashboard');
  const [history, setHistory] = useState<DailyStats[]>([]);
  const [currentSteps, setCurrentSteps] = useState(0);
  const [isTracking, setIsTracking] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [isStandalone, setIsStandalone] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false); // Garde-fou crucial
  const [profile, setProfile] = useState<UserProfile>({
    name: 'Utilisateur',
    goal: 10000,
    weight: 70,
    height: 175
  });

  // 1. Chargement initial des données
  useEffect(() => {
    const standalone = window.matchMedia('(display-mode: standalone)').matches 
                    || (window.navigator as any).standalone;
    setIsStandalone(standalone);

    const savedData = localStorage.getItem(STORAGE_KEY);
    const savedProfile = localStorage.getItem(PROFILE_KEY);
    const savedTheme = localStorage.getItem(THEME_KEY);
    const savedTracking = localStorage.getItem(TRACKING_KEY);
    
    if (savedTheme) setDarkMode(savedTheme === 'dark');
    if (savedProfile) setProfile(JSON.parse(savedProfile));
    
    let initialSteps = 0;
    if (savedData) {
      const parsed = JSON.parse(savedData) as DailyStats[];
      setHistory(parsed);
      const today = new Date().toISOString().split('T')[0];
      const todayData = parsed.find(d => d.date === today);
      if (todayData) {
        initialSteps = todayData.steps;
        setCurrentSteps(todayData.steps);
      }
    }

    if (savedTracking === 'true') {
        // On ne peut pas démarrer automatiquement à cause des permissions iOS/Android
        // mais on prépare l'état pour l'UI
        setIsTracking(false); 
    }

    // On marque l'application comme "prête" seulement après avoir chargé les données
    setTimeout(() => setIsLoaded(true), 100);
  }, []);

  // 2. Gestion du thème sombre
  useEffect(() => {
    if (darkMode) document.documentElement.classList.add('dark');
    else document.documentElement.classList.remove('dark');
    localStorage.setItem(THEME_KEY, darkMode ? 'dark' : 'light');
  }, [darkMode]);

  // 3. Sauvegarde automatique des pas (avec verrou)
  useEffect(() => {
    if (!isLoaded) return; // NE RIEN FAIRE tant que les données ne sont pas chargées

    const today = new Date().toISOString().split('T')[0];
    const distance = Number((currentSteps * 0.0007).toFixed(2));
    const calories = Math.round(currentSteps * 0.04);
    const activeTime = Math.round(currentSteps / 100);

    setHistory(prev => {
      const updated = [...prev];
      const idx = updated.findIndex(h => h.date === today);
      const newDayData = { date: today, steps: currentSteps, distance, calories, activeTime };
      
      if (idx > -1) {
          // On ne met à jour que si le nombre de pas a réellement changé ou augmenté
          // pour éviter les réécritures inutiles au démarrage
          updated[idx] = newDayData;
      } else {
          updated.push(newDayData);
      }
      
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      return updated;
    });
  }, [currentSteps, isLoaded]);

  const lastUpdate = useRef(0);
  const handleMotion = useCallback((event: DeviceMotionEvent) => {
    const acc = event.accelerationIncludingGravity;
    if (!acc) return;
    const total = Math.sqrt((acc.x || 0)**2 + (acc.y || 0)**2 + (acc.z || 0)**2);
    const now = Date.now();
    // Seuil de détection de pas ajusté (12.5) et délai entre deux pas (350ms)
    if (total > 12.5 && now - lastUpdate.current > 350) {
      setCurrentSteps(prev => prev + 1);
      lastUpdate.current = now;
    }
  }, []);

  const toggleTracking = async () => {
    if (!isTracking) {
      try {
        if (typeof (DeviceMotionEvent as any).requestPermission === 'function') {
          const permission = await (DeviceMotionEvent as any).requestPermission();
          if (permission === 'granted') {
            window.addEventListener('devicemotion', handleMotion);
            setIsTracking(true);
            localStorage.setItem(TRACKING_KEY, 'true');
          }
        } else { 
          window.addEventListener('devicemotion', handleMotion);
          setIsTracking(true);
          localStorage.setItem(TRACKING_KEY, 'true');
        }
      } catch (e) {
        console.error("Erreur capteurs:", e);
        alert("ZenStep a besoin de l'accès aux capteurs de mouvement pour compter vos pas.");
      }
    } else {
      window.removeEventListener('devicemotion', handleMotion);
      setIsTracking(false);
      localStorage.setItem(TRACKING_KEY, 'false');
    }
  };

  return (
    <div className={`flex flex-col min-h-screen max-w-md mx-auto bg-white dark:bg-slate-950 shadow-xl relative overflow-hidden transition-colors duration-300 ${isStandalone ? 'pt-safe' : ''}`}>
      <header className="p-6 flex justify-between items-center sticky top-0 z-50 bg-white/80 dark:bg-slate-950/80 backdrop-blur-md">
        <div>
          <h1 className="text-2xl font-black tracking-tight text-gray-900 dark:text-white">ZenStep</h1>
          <p className="text-[10px] text-emerald-500 font-bold uppercase tracking-widest">Podomètre Privé</p>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto pb-32 p-6 no-scrollbar">
        {!isLoaded ? (
            <div className="h-full flex items-center justify-center py-20">
                <div className="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        ) : (
            <>
                {activeView === 'dashboard' && <Dashboard steps={currentSteps} goal={profile.goal} isTracking={isTracking} onToggle={toggleTracking} />}
                {activeView === 'calendar' && <CalendarView history={history} goal={profile.goal} />}
                {activeView === 'history' && <HistoryView history={history} isDark={darkMode} />}
                {activeView === 'settings' && <Settings profile={profile} onUpdate={setProfile} darkMode={darkMode} onToggleDark={() => setDarkMode(!darkMode)} />}
            </>
        )}
      </main>

      <Navigation activeView={activeView} onViewChange={setActiveView} />
    </div>
  );
};

export default App;
