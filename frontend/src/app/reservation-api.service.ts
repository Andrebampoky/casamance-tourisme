import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Booking, CreateBookingRequest, InterestOption, Itinerary, SatisfactionSummary, SurveyQuestion, Ticket, TourismEvent, Visit } from './reservation.models';

@Injectable({ providedIn: 'root' })
export class ReservationApiService {
  private readonly http = inject(HttpClient);

  searchVisits(query: string, category: string, date: string): Observable<Visit[]> {
    let params = new HttpParams();
    if (query.trim()) params = params.set('q', query.trim());
    if (category) params = params.set('category', category);
    if (date) params = params.set('date', date);
    return this.http.get<Visit[]>('/api/visits', { params });
  }

  getVisit(id: number): Observable<Visit> {
    return this.http.get<Visit>(`/api/visits/${id}`);
  }

  createBooking(request: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>('/api/bookings', request);
  }

  getBooking(reference: string): Observable<Booking> {
    return this.http.get<Booking>(`/api/bookings/${encodeURIComponent(reference)}`);
  }

  cancelBooking(reference: string): Observable<Booking> {
    return this.http.post<Booking>(`/api/bookings/${encodeURIComponent(reference)}/cancel`, {});
  }

  getTicket(reference: string): Observable<Ticket> {
    return this.http.get<Ticket>(`/api/tickets/${encodeURIComponent(reference)}`);
  }

  getInterests(): Observable<InterestOption[]> {
    return this.http.get<InterestOption[]>('/api/itineraries/interests');
  }

  createItinerary(request: { interests: string[]; startDate: string; durationDays: number; travelers: number; budget: number }): Observable<Itinerary> {
    return this.http.post<Itinerary>('/api/itineraries', request);
  }

  getItinerary(reference: string): Observable<Itinerary> {
    return this.http.get<Itinerary>(`/api/itineraries/${encodeURIComponent(reference)}`);
  }

  searchEvents(filters: { type: string; date: string; from: string; to: string; location: string; q: string }): Observable<TourismEvent[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => { if (value.trim()) params = params.set(key, value.trim()); });
    return this.http.get<TourismEvent[]>('/api/events', { params });
  }

  getSurveyQuestions(reference: string): Observable<SurveyQuestion[]> {
    return this.http.get<SurveyQuestion[]>(`/api/satisfaction/reservations/${encodeURIComponent(reference)}/questions`);
  }

  submitSurvey(reference: string, answers: { questionId: number; value: string }[]): Observable<{ reservationReference: string; answersSaved: number; submittedAt: string }> {
    return this.http.post<{ reservationReference: string; answersSaved: number; submittedAt: string }>(
      `/api/satisfaction/reservations/${encodeURIComponent(reference)}`, { answers });
  }

  adminBookings(token: string): Observable<Booking[]> {
    return this.http.get<Booking[]>('/api/admin/bookings', { headers: this.adminHeaders(token) });
  }

  confirmBooking(token: string, reference: string): Observable<Booking> {
    return this.http.post<Booking>(`/api/admin/bookings/${encodeURIComponent(reference)}/confirm`, {}, { headers: this.adminHeaders(token) });
  }

  createEvent(token: string, event: Omit<TourismEvent, 'id'>): Observable<TourismEvent> {
    return this.http.post<TourismEvent>('/api/admin/events', event, { headers: this.adminHeaders(token) });
  }

  adminQuestions(token: string): Observable<SurveyQuestion[]> {
    return this.http.get<SurveyQuestion[]>('/api/admin/satisfaction/questions', { headers: this.adminHeaders(token) });
  }

  createSurveyQuestion(token: string, question: { prompt: string; type: SurveyQuestion['type']; options: string; displayOrder: number }): Observable<SurveyQuestion> {
    return this.http.post<SurveyQuestion>('/api/admin/satisfaction/questions', question, { headers: this.adminHeaders(token) });
  }

  setQuestionActive(token: string, id: number, active: boolean): Observable<void> {
    return this.http.patch<void>(`/api/admin/satisfaction/questions/${id}/active?active=${active}`, {}, { headers: this.adminHeaders(token) });
  }

  satisfactionStatistics(token: string, visitId: string, from: string, to: string): Observable<SatisfactionSummary[]> {
    let params = new HttpParams();
    if (visitId) params = params.set('visitId', visitId);
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<SatisfactionSummary[]>('/api/admin/satisfaction/statistics', { headers: this.adminHeaders(token), params });
  }

  private adminHeaders(token: string): HttpHeaders {
    return new HttpHeaders({ 'X-Admin-Token': token });
  }
}
