
import React, { useState } from 'react';
import { DailyStats } from '../types';

interface Props {
  history: DailyStats[];
  goal: number;
}

const CalendarView: React.FC<Props> = ({ history, goal }) => {
  const [viewMode, setViewMode] = useState<'year' | 'week'>('week');
  const [weekOffset, setWeekOffset] = useState(0);

  const currentYear = new Date().getFullYear();
  const months = [
    'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
  ];

  const getDayStats = (dateStr: string) => {
    return history.find(h => h.date === dateStr);
  };

  // --- LOGIQUE VUE ANNUELLE (Heatmap) ---
  const getColorClass = (steps: number) => {
    if (steps === 0) return 'bg-gray-100 dark:bg-slate-800';
    const ratio = steps / goal;
    if (ratio >= 1) return 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.4)]';
    if (ratio >= 0.7) return 'bg-emerald-400';
    if (ratio >= 0.4) return 'bg-emerald-300';
    if (ratio >= 0.1) return 'bg-emerald-200 dark:bg-emerald-900/40';
    return 'bg-emerald-100 dark:bg-emerald-950/40';
  };

  const renderMonth = (monthIndex: number) => {
    const firstDay = new Date(currentYear, monthIndex, 1);
    const lastDay = new Date(currentYear, monthIndex + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = (firstDay.getDay() + 6) % 7;

    const days = [];
    for (let i = 0; i < startDayOfWeek; i++) {
      days.push(<div key={`empty-${i}`} className="w-full aspect-square" />);
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const date = new Date(currentYear, monthIndex, d);
      const dateStr = date.toISOString().split('T')[0];
      const stats = getDayStats(dateStr);
      const steps = stats ? stats.steps : 0;

      days.push(
        <div 
          key={dateStr} 
          className={`w-full aspect-square rounded-[4px] transition-all duration-300 ${getColorClass(steps)}`}
          title={`${dateStr}: ${steps} pas`}
        />
      );
    }

    return (
      <div key={months[monthIndex]} className="bg-white dark:bg-slate-900 p-4 rounded-3xl border border-gray-100 dark:border-slate-800 shadow-sm transition-colors">
        <h4 className="text-sm font-bold text-gray-900 dark:text-white mb-3 flex justify-between items-center">
          {months[monthIndex]}
          <span className="text-[10px] font-black text-emerald-500 uppercase tracking-tighter">
            {history.filter(h => h.date.startsWith(`${currentYear}-${String(monthIndex + 1).padStart(2, '0')}`) && h.steps >= goal).length} Objectifs
          </span>
        </h4>
        <div className="grid grid-cols-7 gap-1.5">
          {['L', 'M', 'M', 'J', 'V', 'S', 'D'].map(day => (
            <div key={day} className="text-[8px] font-bold text-gray-300 dark:text-slate-700 text-center mb-1">
              {day}
            </div>
          ))}
          {days}
        </div>
      </div>
    );
  };

  // --- LOGIQUE VUE HEBDOMADAIRE (Précise) ---
  const getWeekDays = () => {
    const today = new Date();
    const day = today.getDay() || 7; // Lundi = 1, Dimanche = 7
    const monday = new Date(today);
    monday.setDate(today.getDate() - (day - 1) + (weekOffset * 7));
    
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      return d;
    });
  };

  const weekDays = getWeekDays();
  const weekStats = weekDays.map(d => {
    const dateStr = d.toISOString().split('T')[0];
    return {
      date: d,
      dateStr,
      stats: getDayStats(dateStr)
    };
  });

  const totalStepsWeek = weekStats.reduce((acc, curr) => acc + (curr.stats?.steps || 0), 0);
  const avgStepsWeek = Math.round(totalStepsWeek / 7);

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      {/* Header avec Switcher */}
      <div className="bg-white dark:bg-slate-900 p-2 rounded-[2rem] shadow-sm border border-gray-100 dark:border-slate-800 flex transition-colors">
        <button 
          onClick={() => setViewMode('week')}
          className={`flex-1 py-3 rounded-full text-xs font-bold uppercase tracking-wider transition-all ${viewMode === 'week' ? 'bg-emerald-500 text-white shadow-md' : 'text-gray-400'}`}
        >
          Semaine
        </button>
        <button 
          onClick={() => setViewMode('year')}
          className={`flex-1 py-3 rounded-full text-xs font-bold uppercase tracking-wider transition-all ${viewMode === 'year' ? 'bg-emerald-500 text-white shadow-md' : 'text-gray-400'}`}
        >
          Année
        </button>
      </div>

      {viewMode === 'year' ? (
        <>
          <div className="bg-emerald-600 dark:bg-emerald-700 p-6 rounded-[2.5rem] text-white shadow-xl shadow-emerald-100 dark:shadow-none transition-colors">
            <h3 className="text-emerald-100 font-bold uppercase tracking-wider text-xs mb-2">Vision Annuelle {currentYear}</h3>
            <p className="text-2xl font-black">Ta régularité</p>
            <div className="mt-4 flex gap-4 items-center overflow-x-auto no-scrollbar pb-1">
              <div className="flex items-center gap-2 whitespace-nowrap">
                <div className="w-3 h-3 rounded-[2px] bg-gray-100 dark:bg-slate-800" />
                <span className="text-[10px] font-bold opacity-70">0%</span>
              </div>
              <div className="flex items-center gap-2 whitespace-nowrap">
                <div className="w-3 h-3 rounded-[2px] bg-emerald-300" />
                <span className="text-[10px] font-bold opacity-70">50%</span>
              </div>
              <div className="flex items-center gap-2 whitespace-nowrap">
                <div className="w-3 h-3 rounded-[2px] bg-emerald-500 shadow-sm" />
                <span className="text-[10px] font-bold opacity-70">100%+</span>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-1 gap-4">
            {months.map((_, i) => renderMonth(i))}
          </div>
        </>
      ) : (
        <div className="space-y-6">
          {/* Navigation Semaine */}
          <div className="flex items-center justify-between px-2">
            <button 
              onClick={() => setWeekOffset(prev => prev - 1)}
              className="w-10 h-10 rounded-full bg-white dark:bg-slate-900 border border-gray-100 dark:border-slate-800 flex items-center justify-center text-gray-600 dark:text-white active:scale-90 transition-all shadow-sm"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 19l-7-7 7-7" /></svg>
            </button>
            <div className="text-center">
              <p className="text-xs font-bold text-emerald-500 uppercase tracking-widest">Semaine du</p>
              <p className="text-sm font-black text-gray-900 dark:text-white">
                {weekDays[0].toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })} - {weekDays[6].toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })}
              </p>
            </div>
            <button 
              onClick={() => setWeekOffset(prev => prev + 1)}
              className="w-10 h-10 rounded-full bg-white dark:bg-slate-900 border border-gray-100 dark:border-slate-800 flex items-center justify-center text-gray-600 dark:text-white active:scale-90 transition-all shadow-sm"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>

          {/* Résumé Semaine */}
          <div className="bg-emerald-600 dark:bg-emerald-700 p-6 rounded-[2.5rem] text-white shadow-xl shadow-emerald-100 dark:shadow-none transition-colors">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-3xl font-black">{totalStepsWeek.toLocaleString()}</p>
                <p className="text-[10px] text-emerald-100 font-bold uppercase opacity-80">Total pas / semaine</p>
              </div>
              <div>
                <p className="text-3xl font-black">{avgStepsWeek.toLocaleString()}</p>
                <p className="text-[10px] text-emerald-100 font-bold uppercase opacity-80">Moyenne quotidienne</p>
              </div>
            </div>
          </div>

          {/* Liste détaillée des jours */}
          <div className="space-y-4">
            {weekStats.map(({ date, dateStr, stats }, idx) => {
              const steps = stats?.steps || 0;
              const progress = Math.min((steps / goal) * 100, 100);
              const isToday = new Date().toISOString().split('T')[0] === dateStr;

              return (
                <div key={dateStr} className={`bg-white dark:bg-slate-900 p-5 rounded-[2rem] border transition-all ${isToday ? 'border-emerald-500 shadow-md ring-1 ring-emerald-500/20' : 'border-gray-100 dark:border-slate-800'}`}>
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <p className={`text-[10px] font-black uppercase tracking-widest ${isToday ? 'text-emerald-500' : 'text-gray-400 dark:text-slate-500'}`}>
                        {date.toLocaleDateString('fr-FR', { weekday: 'long' })} {isToday ? "(Aujourd'hui)" : ""}
                      </p>
                      <p className="text-lg font-black text-gray-900 dark:text-white">
                        {steps.toLocaleString()} <span className="text-xs font-medium text-gray-400">pas</span>
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs font-bold text-gray-900 dark:text-white">{stats?.distance || 0} km</p>
                      <p className="text-[10px] text-gray-400 dark:text-slate-500 font-bold">{stats?.calories || 0} kcal</p>
                    </div>
                  </div>
                  
                  {/* Barre de progression miniature */}
                  <div className="h-2 bg-gray-50 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div 
                      className={`h-full rounded-full transition-all duration-1000 ${steps >= goal ? 'bg-emerald-500' : 'bg-emerald-300'}`}
                      style={{ width: `${progress}%` }}
                    />
                  </div>
                  
                  {steps >= goal && (
                    <div className="flex items-center gap-1 mt-2 text-emerald-500">
                      <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" /></svg>
                      <span className="text-[9px] font-black uppercase tracking-tighter">Objectif atteint !</span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
      
      <div className="pb-8 text-center">
        <p className="text-xs text-gray-400 dark:text-slate-600 font-medium italic">
          {viewMode === 'year' ? 'Chaque carré représente un jour de ta transformation.' : 'Détail quotidien de ta performance hebdomadaire.'}
        </p>
      </div>
    </div>
  );
};

export default CalendarView;
