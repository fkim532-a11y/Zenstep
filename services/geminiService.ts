
import { GoogleGenAI } from "@google/genai";
import { DailyStats } from "../types";

/**
 * Helper to delay execution for retries
 */
const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const getAICoaching = async (stats: DailyStats[], userName: string, retryCount = 0): Promise<string> => {
  // Always create a fresh instance to ensure the latest API key is used
  const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
  
  const last7Days = stats.slice(-7);
  const avgSteps = last7Days.reduce((acc, curr) => acc + curr.steps, 0) / (last7Days.length || 1);
  const totalKm = last7Days.reduce((acc, curr) => acc + curr.distance, 0);

  const systemInstruction = `Tu es ZenBot, un coach sportif personnel bienveillant et énergique. 
Analyse les données de marche de l'utilisateur et donne un conseil court, motivant et précis.
Inclus toujours une petite anecdote de santé intéressante. 
Réponds en français. Ton max: 120 mots.`;

  const userPrompt = `Analyse mes 7 derniers jours :
- Moyenne de pas : ${Math.round(avgSteps)}/jour
- Distance totale : ${totalKm.toFixed(2)} km
- Objectif quotidien : 10 000 pas.`;

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: userPrompt,
      config: {
        systemInstruction: systemInstruction,
        temperature: 0.7,
        topP: 0.9,
      },
    });

    if (!response.text) {
      throw new Error("Empty response from Gemini");
    }

    return response.text;
  } catch (error: any) {
    console.error(`Gemini Attempt ${retryCount + 1} failed:`, error);

    // Exponential backoff for 500 errors or network failures
    if (retryCount < 2) {
      const waitTime = Math.pow(2, retryCount) * 1000;
      await sleep(waitTime);
      return getAICoaching(stats, userName, retryCount + 1);
    }

    // Fallback if all retries fail
    throw error;
  }
};
