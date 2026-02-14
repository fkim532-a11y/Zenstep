
import React from 'react';

interface Props {
  steps: number;
  goal: number;
  isTracking: boolean;
  onToggle: () => void;
}

const Dashboard: React.FC<Props> = ({ steps, goal, isTracking, onToggle }) => {
  const percentage = Math.min((steps / goal) * 100, 100);
  const distance = (steps * 0.0007).toFixed(2);
  const calories = Math.round(steps * 0.04);
  const time = Math.round(steps / 100);

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex justify-center py-4 relative">
        <div className="relative w-64 h-64 flex items-center justify-center">
          <svg className="w-full h-full transform -rotate-90" viewBox="0 0 256 256">
            <circle
              cx="128"
              cy="128"
              r="112"
              stroke="currentColor"
              strokeWidth="14"
              fill="transparent"
              className="text-gray-100 dark:text-slate-800 transition-colors"
            />
            <circle
              cx="128"
              cy="128"
              r="112"
              stroke="currentColor"
              strokeWidth="14"
              fill="transparent"
              strokeDasharray={2 * Math.PI * 112}
              strokeDashoffset={2 * Math.PI * 112 * (1 - percentage / 100)}
              strokeLinecap="round"
              className="text-emerald-500 dark:text-emerald-400 transition-all duration-1000 ease-out"
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-5xl font-black text-gray-900 dark:text-white transition-colors">{steps.toLocaleString()}</span>
            <span className="text-xs text-gray-400 dark:text-slate-500 font-bold uppercase tracking-widest mt-1">Pas Aujourd'hui</span>
            <div className="mt-4 bg-emerald-50 dark:bg-emerald-900/30 px-3 py-1 rounded-full border border-emerald-100 dark:border-emerald-800/50">
                <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-bold">Objectif: {goal}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-3">
        {[
            { label: 'KM', val: distance, icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z', color: 'blue' },
            { label: 'Kcal', val: calories, icon: 'M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.99 7.99 0 0120 13a7.99 7.99 0 01-2.343 5.657z', color: 'orange' },
            { label: 'Mins', val: time, icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z', color: 'emerald' }
        ].map(stat => (
            <div key={stat.label} className="bg-white dark:bg-slate-900 p-4 rounded-3xl border border-gray-100 dark:border-slate-800 shadow-sm flex flex-col items-center transition-all">
                <div className={`w-10 h-10 rounded-2xl bg-${stat.color}-50 dark:bg-${stat.color}-900/20 flex items-center justify-center mb-2`}>
                    <svg className={`w-5 h-5 text-${stat.color}-500 dark:text-${stat.color}-400`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={stat.icon} />
                    </svg>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white leading-none">{stat.val}</span>
                <span className="text-[10px] text-gray-400 dark:text-slate-500 font-bold uppercase tracking-wider mt-1">{stat.label}</span>
            </div>
        ))}
      </div>

      <div className="pt-2">
        <button
          onClick={onToggle}
          className={`w-full py-5 rounded-3xl font-bold text-lg transition-all active:scale-95 flex items-center justify-center gap-3 shadow-lg ${
            isTracking 
              ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 border-2 border-red-100 dark:border-red-900/30' 
              : 'bg-emerald-500 text-white shadow-emerald-200 dark:shadow-none'
          }`}
        >
          {isTracking ? 'Arrêter le suivi' : 'Démarrer le suivi'}
        </button>
      </div>
    </div>
  );
};

export default Dashboard;
