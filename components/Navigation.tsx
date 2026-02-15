
import React from 'react';
import { ViewType } from '../types';

interface Props {
  activeView: ViewType;
  onViewChange: (view: ViewType) => void;
}

const Navigation: React.FC<Props> = ({ activeView, onViewChange }) => {
  const items: { id: ViewType; icon: React.ReactNode; label: string }[] = [
    { 
      id: 'dashboard', 
      label: 'Stats',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
    },
    { 
      id: 'calendar', 
      label: 'Calendrier',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2-2v12a2 2 0 002 2z" />
    },
    { 
      id: 'history', 
      label: 'Historique',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
    },
    { 
      id: 'settings', 
      label: 'Profil',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    },
  ];

  return (
    <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-md bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl border-t border-gray-100 dark:border-slate-800 px-2 py-4 z-50 rounded-t-[2.5rem] shadow-2xl">
      <div className="flex justify-around items-center">
        {items.map((item) => (
          <button
            key={item.id}
            onClick={() => onViewChange(item.id)}
            className={`flex flex-col items-center gap-1.5 transition-all flex-1 ${
              activeView === item.id ? 'text-emerald-600 dark:text-emerald-400 scale-105' : 'text-gray-400 dark:text-slate-600'
            }`}
          >
            <div className={`p-2 rounded-2xl ${activeView === item.id ? 'bg-emerald-50 dark:bg-emerald-900/20' : ''}`}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                {item.icon}
              </svg>
            </div>
            <span className="text-[8px] font-bold uppercase tracking-tighter">{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default Navigation;
