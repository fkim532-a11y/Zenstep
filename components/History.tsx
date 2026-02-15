
import React from 'react';
import { BarChart, Bar, XAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { DailyStats } from '../types';

interface Props {
  history: DailyStats[];
  isDark: boolean;
}

const HistoryView: React.FC<Props> = ({ history, isDark }) => {
  const chartData = history.slice(-7).map(h => ({
    name: new Date(h.date).toLocaleDateString('fr-FR', { weekday: 'short' }),
    steps: h.steps,
    fullDate: h.date
  }));

  const avgSteps = Math.round(history.reduce((acc, curr) => acc + curr.steps, 0) / (history.length || 1));
  const totalDistance = history.reduce((acc, curr) => acc + curr.distance, 0).toFixed(1);

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="bg-emerald-600 dark:bg-emerald-700 p-6 rounded-[2.5rem] text-white shadow-xl shadow-emerald-100 dark:shadow-none transition-colors">
        <h3 className="text-emerald-100 font-bold uppercase tracking-wider text-xs mb-4">Statistiques Hebdomadaires</h3>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-3xl font-black">{avgSteps.toLocaleString()}</p>
            <p className="text-xs text-emerald-100 font-medium opacity-80">Moyenne pas / jour</p>
          </div>
          <div>
            <p className="text-3xl font-black">{totalDistance} km</p>
            <p className="text-xs text-emerald-100 font-medium opacity-80">Total parcouru</p>
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-900 p-4 rounded-3xl border border-gray-100 dark:border-slate-800 shadow-sm overflow-hidden">
        <h4 className="font-bold text-gray-900 dark:text-white mb-6 px-2">Activité des 7 derniers jours</h4>
        <div className="h-64 w-full min-w-0 relative">
          <ResponsiveContainer width="100%" height="100%" minWidth={0} minHeight={0}>
            <BarChart data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 20 }}>
              <XAxis 
                dataKey="name" 
                axisLine={false} 
                tickLine={false} 
                tick={{ fontSize: 12, fill: isDark ? '#64748b' : '#94a3b8' }} 
                dy={10} 
              />
              <Tooltip 
                cursor={{ fill: 'transparent' }}
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    return (
                      <div className="bg-white dark:bg-slate-800 p-3 shadow-xl rounded-2xl border border-gray-50 dark:border-slate-700">
                        <p className="text-xs font-bold text-gray-400 dark:text-slate-500 mb-1">{payload[0].payload.fullDate}</p>
                        <p className="text-lg font-black text-emerald-600 dark:text-emerald-400">{payload[0].value?.toLocaleString()} pas</p>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <Bar dataKey="steps" radius={[10, 10, 10, 10]} barSize={32}>
                {chartData.map((entry, index) => (
                  <Cell 
                    key={`cell-${index}`} 
                    fill={index === chartData.length - 1 ? (isDark ? '#34d399' : '#10b981') : (isDark ? '#1e293b' : '#e2e8f0')} 
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="space-y-3">
        <h4 className="font-bold text-gray-900 dark:text-white px-2">Historique récent</h4>
        {history.slice().reverse().map((day) => (
          <div key={day.date} className="bg-white dark:bg-slate-900 p-4 rounded-3xl border border-gray-100 dark:border-slate-800 flex items-center justify-between transition-colors">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-gray-50 dark:bg-slate-800 flex items-center justify-center font-bold text-gray-400 dark:text-slate-600 text-xs">
                {new Date(day.date).getDate()}/{new Date(day.date).getMonth() + 1}
              </div>
              <div>
                <p className="font-bold text-gray-900 dark:text-white">{day.steps.toLocaleString()} pas</p>
                <p className="text-xs text-gray-400 dark:text-slate-500 font-semibold">{day.distance} km • {day.calories} kcal</p>
              </div>
            </div>
            {day.steps >= 10000 && (
              <div className="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900/30 flex items-center justify-center text-yellow-600 dark:text-yellow-400">
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default HistoryView;
