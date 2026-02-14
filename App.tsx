
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

const App: React.FC = () => {
  const [activeView, setActiveView] = useState<ViewType>('dashboard');
  const [history, setHistory] = useState<DailyStats[]>([]);
  const [currentSteps, setCurrentSteps] = useState(0);
  const [isTracking, setIsTracking] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [isStandalone, setIsStandalone] = useState(false);
  const [profile, setProfile] = useState<UserProfile>({
    name: 'Utilisateur',
    goal: 10000,
    weight: 70,
    height: 175
  });

  useEffect(() => {
    // Détection PWA
    const standalone = window.matchMedia('(display-mode: standalone)').matches 
                    || (window.navigator as any).standalone;
    setIsStandalone(standalone);

    // Chargement des données locales
    const savedData = localStorage.getItem(STORAGE_KEY);
    const savedProfile = localStorage.getItem(PROFILE_KEY);
    const savedTheme = localStorage.getItem(THEME_KEY);
    
    if (savedTheme) setDarkMode(savedTheme === 'dark');
    if (savedProfile) setProfile(JSON.parse(savedProfile));
    
    if (savedData) {
      const parsed = JSON.parse(savedData) as DailyStats[];
      setHistory(parsed);
      const today = new Date().toISOString().split('T')[0];
      const todayData = parsed.find(d => d.date === today);
      if (todayData) setCurrentSteps(todayData.steps);
    }
  }, []);

  useEffect(() => {
    if (darkMode) document.documentElement.classList.add('dark');
    else document.documentElement.classList.remove('dark');
    localStorage.setItem(THEME_KEY, darkMode ? 'dark' : 'light');
  }, [darkMode]);

  useEffect(() => {
    const today = new Date().toISOString().split('T')[0];
    const distance = Number((currentSteps * 0.0007).toFixed(2));
    const calories = Math.round(currentSteps * 0.04);
    const activeTime = Math.round(currentSteps / 100);

    const updatedHistory = [...history];
    const todayIdx = updatedHistory.findIndex(h => h.date === today);
    const newDayData: DailyStats = { date: today, steps: currentSteps, distance, calories, activeTime };

    if (todayIdx > -1) updatedHistory[todayIdx] = newDayData;
    else updatedHistory.push(newDayData);

    setHistory(updatedHistory);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedHistory));
  }, [currentSteps]);

  const lastUpdate = useRef(0);
  const handleMotion = useCallback((event: DeviceMotionEvent) => {
    const acc = event.accelerationIncludingGravity;
    if (!acc) return;
    const total = Math.sqrt((acc.x || 0)**2 + (acc.y || 0)**2 + (acc.z || 0)**2);
    const now = Date.now();
    // Seuil de détection optimisé pour smartphone Android/iOS
    if (total > 12.5 && now - lastUpdate.current > 350) {
      setCurrentSteps(prev => prev + 1);
      lastUpdate.current = now;
    }
  }, []);

  const toggleTracking = async () => {
    if (!isTracking) {
      if (typeof (DeviceMotionEvent as any).requestPermission === 'function') {
        try {
          const permission = await (DeviceMotionEvent as any).requestPermission();
          if (permission === 'granted') {
            window.addEventListener('devicemotion', handleMotion);
            setIsTracking(true);
          }
        } catch (e) { console.error(e); }
      } else { 
        window.addEventListener('devicemotion', handleMotion);
        setIsTracking(true);
      }
    } else {
      window.removeEventListener('devicemotion', handleMotion);
      setIsTracking(false);
    }
  };

  const toggleDarkMode = () => setDarkMode(!darkMode);

  return (
    <div className={`flex flex-col min-h-screen max-w-md mx-auto bg-white dark:bg-slate-950 shadow-xl relative overflow-hidden transition-colors duration-300 ${isStandalone ? 'pt-safe' : ''}`}>
      <header className={`p-6 flex justify-between items-center bg-white/80 dark:bg-slate-950/80 backdrop-blur-md sticky top-0 z-50 transition-colors ${isStandalone ? 'pt-10' : ''}`}>
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-gray-900 dark:text-white">ZenStep</h1>
          <p className="text-[10px] text-emerald-500 font-bold uppercase tracking-widest">
            {isStandalone ? 'Mode App Installée' : 'Podomètre Privé'}
          </p>
        </div>
        <button 
          onClick={toggleDarkMode}
          className="w-10 h-10 rounded-2xl bg-gray-50 dark:bg-slate-900 flex items-center justify-center text-gray-600 dark:text-slate-400 border border-gray-100 dark:border-slate-800 transition-all active:scale-90"
        >
          {darkMode ? (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364-6.364l-.707.707M6.343 17.657l-.707.707M16.243 16.243l.707.707M7.757 7.757l.707.707M12 8a4 4 0 100 8 4 4 0 000-8z" /></svg>
          ) : (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>
          )}
        </button>
      </header>

      <main className="flex-1 overflow-y-auto pb-32 p-6 no-scrollbar">
        {activeView === 'dashboard' && <Dashboard steps={currentSteps} goal={profile.goal} isTracking={isTracking} onToggle={toggleTracking} />}
        {activeView === 'calendar' && <CalendarView history={history} goal={profile.goal} />}
        {activeView === 'history' && <HistoryView history={history} isDark={darkMode} />}
        {activeView === 'settings' && <Settings profile={profile} onUpdate={setProfile} darkMode={darkMode} onToggleDark={toggleDarkMode} />}
      </main>

      <Navigation activeView={activeView} onViewChange={setActiveView} />
    </div>
  );
};

export default App;
