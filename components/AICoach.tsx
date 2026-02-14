
import React, { useState, useEffect, useCallback } from 'react';
import { DailyStats } from '../types';
import { getAICoaching } from '../services/geminiService';

interface Props {
  history: DailyStats[];
  userName: string;
}

const AICoach: React.FC<Props> = ({ history, userName }) => {
  const [advice, setAdvice] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<boolean>(false);

  const fetchAdvice = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      const res = await getAICoaching(history, userName);
      setAdvice(res);
    } catch (err) {
      console.error("Failed to fetch AI advice:", err);
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [userName]); // On ne dépend plus de history pour éviter les rechargements à chaque pas

  useEffect(() => {
    fetchAdvice();
  }, []); // Chargement initial au montage du composant uniquement

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="bg-indigo-600 dark:bg-indigo-700 p-8 rounded-[2.5rem] text-white relative overflow-hidden shadow-xl shadow-indigo-100 dark:shadow-none min-h-[220px] flex flex-col justify-center transition-colors">
        <div className="absolute top-0 right-0 p-4 opacity-10">
          <svg className="w-32 h-32" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
          </svg>
        </div>
        
        <div className="flex justify-between items-start mb-4 relative z-10">
            <div>
                <h3 className="text-indigo-100 font-bold uppercase tracking-widest text-[10px] mb-1">Coach ZenBot IA</h3>
                <h2 className="text-3xl font-black">Conseil du jour</h2>
            </div>
            {!loading && !error && (
                <button 
                    onClick={fetchAdvice}
                    className="p-2 bg-indigo-500/30 rounded-full hover:bg-indigo-500/50 transition-colors"
                    title="Actualiser le conseil"
                >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                </button>
            )}
        </div>
        
        {loading ? (
          <div className="space-y-3 relative z-10">
            <div className="h-4 bg-indigo-500/50 rounded animate-pulse w-3/4"></div>
            <div className="h-4 bg-indigo-500/50 rounded animate-pulse w-full"></div>
            <div className="h-4 bg-indigo-500/50 rounded animate-pulse w-2/3"></div>
          </div>
        ) : error ? (
          <div className="space-y-4 relative z-10">
            <p className="text-indigo-100 text-sm font-medium">Oups ! ZenBot a eu un petit problème technique.</p>
            <button 
              onClick={fetchAdvice}
              className="bg-white text-indigo-600 px-6 py-2 rounded-full font-bold text-sm hover:bg-indigo-50 transition-colors shadow-lg active:scale-95"
            >
              Réessayer
            </button>
          </div>
        ) : (
          <p className="text-lg font-medium leading-relaxed italic animate-in fade-in duration-700 relative z-10">
            "{advice}"
          </p>
        )}
      </div>

      <div className="bg-white dark:bg-slate-900 p-6 rounded-3xl border border-gray-100 dark:border-slate-800 shadow-sm space-y-4 transition-colors">
        <h4 className="font-bold text-gray-900 dark:text-white">Pourquoi ZenStep ?</h4>
        <div className="grid grid-cols-1 gap-4">
          <div className="flex gap-4 items-start">
            <div className="w-10 h-10 rounded-2xl bg-emerald-50 dark:bg-emerald-900/20 flex-shrink-0 flex items-center justify-center text-emerald-600 dark:text-emerald-400">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p className="font-bold text-sm text-gray-900 dark:text-white">Santé Cardiaque</p>
              <p className="text-xs text-gray-500 dark:text-slate-500 mt-1">Marcher 30 min par jour réduit les risques cardiaques de 40%.</p>
            </div>
          </div>
          <div className="flex gap-4 items-start">
            <div className="w-10 h-10 rounded-2xl bg-blue-50 dark:bg-blue-900/20 flex-shrink-0 flex items-center justify-center text-blue-600 dark:text-blue-400">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <p className="font-bold text-sm text-gray-900 dark:text-white">Énergie & Humeur</p>
              <p className="text-xs text-gray-500 dark:text-slate-500 mt-1">L'exercice libère des endorphines, améliorant votre bien-être mental.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AICoach;
