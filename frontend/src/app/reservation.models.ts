export interface Visit {
  id: number;
  title: string;
  description: string;
  location: string;
  category: string;
  duration: string;
  price: number;
  image: string;
  rating: number;
  capacity: number;
}

export interface Booking {
  id: number;
  reference: string;
  visitTitle: string;
  location: string;
  image: string;
  visitDate: string;
  travelers: number;
  total: number;
  status: string;
}

export interface CreateBookingRequest {
  visitId: number;
  customerName: string;
  email: string;
  visitDate: string;
  travelers: number;
}

export interface InterestOption {
  code: string;
  label: string;
  description: string;
  icon: string;
}

export interface ItineraryStop {
  dayNumber: number;
  stopOrder: number;
  visitId: number;
  title: string;
  location: string;
  duration: string;
  estimatedCost: number;
}

export interface Itinerary {
  reference: string;
  startDate: string;
  endDate: string;
  durationDays: number;
  travelers: number;
  interests: string[];
  budget: number;
  estimatedTotal: number;
  budgetRemaining: number;
  currency: string;
  stops: ItineraryStop[];
}

export interface TourismEvent {
  id: number;
  title: string;
  description: string;
  type: string;
  location: string;
  startsAt: string;
  endsAt: string;
  practicalInfo: string;
  imageUrl: string;
}

export interface Ticket {
  reservationReference: string;
  activity: string;
  location: string;
  visitDate: string;
  participants: number;
  total: number;
  status: string;
  qrSvgDataUri: string;
}

export interface SurveyQuestion {
  id: number;
  prompt: string;
  type: 'SCALE' | 'CHOICE' | 'YES_NO' | 'TEXT';
  options: string[];
  displayOrder: number;
  active: boolean;
}

export interface SatisfactionSummary {
  questionId: number;
  prompt: string;
  visitId: number;
  activity: string;
  period: string;
  answerCount: number;
  averageScore: number | null;
  answerCounts: Record<string, number>;
}
