
export interface DailyStats {
  date: string;
  steps: number;
  calories: number;
  distance: number; // en km
  activeTime: number; // en minutes
}

export type ViewType = 'dashboard' | 'calendar' | 'history' | 'settings';

export interface UserProfile {
  name: string;
  goal: number;
  weight: number; // kg
  height: number; // cm
}
