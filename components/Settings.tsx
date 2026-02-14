
import React from 'react';
import { UserProfile } from '../types';

interface Props {
  profile: UserProfile;
  onUpdate: (profile: UserProfile) => void;
  darkMode: boolean;
  onToggleDark: () => void;
}

const Settings: React.FC<Props> = ({ profile, onUpdate, darkMode, onToggleDark }) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const newProfile = { 
      ...profile, 
      [name]: name === 'name' ? value : Number(value) 
    };
    onUpdate(newProfile);
    localStorage.setItem('zenstep_profile', JSON.stringify(newProfile));
  };

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="bg-white dark:bg-slate-900 p-6 rounded-3xl border border-gray-100 dark:border-slate-800 shadow-sm space-y-6 transition-colors">
        <h3 className="text-xl font-black text-gray-900 dark:text-white">Configuration du Profil</h3>
        
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-transparent dark:border-slate-800">
            <div>
              <p className="text-sm font-bold text-gray-900 dark:text-white">Mode Sombre</p>
              <p className="text-xs text-gray-400 dark:text-slate-500">Réduire la fatigue visuelle</p>
            </div>
            <button 
              onClick={onToggleDark}
              className={`w-12 h-6 rounded-full transition-all relative ${darkMode ? 'bg-emerald-500' : 'bg-gray-200'}`}
            >
              <div className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${darkMode ? 'left-7' : 'left-1'}`} />
            </button>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 px-1">Nom complet</label>
            <input 
              type="text" 
              name="name"
              value={profile.name}
              onChange={handleChange}
              className="w-full bg-gray-50 dark:bg-slate-800 border-none rounded-2xl px-4 py-3 font-bold text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 px-1">Objectif de pas</label>
            <input 
              type="number" 
              name="goal"
              value={profile.goal}
              onChange={handleChange}
              className="w-full bg-gray-50 dark:bg-slate-800 border-none rounded-2xl px-4 py-3 font-bold text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 px-1">Poids (kg)</label>
              <input 
                type="number" 
                name="weight"
                value={profile.weight}
                onChange={handleChange}
                className="w-full bg-gray-50 dark:bg-slate-800 border-none rounded-2xl px-4 py-3 font-bold text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 transition-all"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 px-1">Taille (cm)</label>
              <input 
                type="number" 
                name="height"
                value={profile.height}
                onChange={handleChange}
                className="w-full bg-gray-50 dark:bg-slate-800 border-none rounded-2xl px-4 py-3 font-bold text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 transition-all"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="bg-emerald-50 dark:bg-emerald-900/20 p-6 rounded-3xl border border-emerald-100 dark:border-emerald-900/30">
        <p className="text-sm font-bold text-emerald-800 dark:text-emerald-400 mb-2">Astuce ZenStep</p>
        <p className="text-xs text-emerald-600 dark:text-emerald-500 leading-relaxed font-medium">
          Vos données de santé sont stockées uniquement sur cet appareil. Changez vos objectifs pour plus de challenge !
        </p>
      </div>

      <div className="p-4 text-center">
        <p className="text-[10px] text-gray-300 dark:text-slate-700 font-bold uppercase tracking-widest">Version 1.1.0 • ZenStep Mobile Web</p>
      </div>
    </div>
  );
};

export default Settings;
