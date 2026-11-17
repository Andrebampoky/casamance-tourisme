import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ReservationApiService } from './reservation-api.service';
import { Booking, InterestOption, Itinerary, SatisfactionSummary, SurveyQuestion, Ticket, TourismEvent, Visit } from './reservation.models';

type Page = 'home' | 'detail' | 'booking' | 'confirmation' | 'reservations' | 'tickets' | 'itineraries' | 'events' | 'survey' | 'admin';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  private readonly reservationApi = inject(ReservationApiService);
  readonly imageUrls: Record<string, string> = {
    river: 'https://images.unsplash.com/photo-1500375592092-40eb2168fd21?auto=format&fit=crop&w=1000&q=85',
    forest: 'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1000&q=85',
    church: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1000&q=85',
    beach: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1000&q=85',
  };
  readonly today = this.localDate(new Date());
  readonly nav = [
    { label: 'Accueil', icon: '⌂', page: 'home' as Page },
    { label: 'Découvrir', icon: '⌕', page: 'home' as Page },
    { label: 'Réserver', icon: '▦', page: 'home' as Page },
    { label: 'Mes réservations', icon: '▤', page: 'reservations' as Page },
    { label: 'Mes billets', icon: '▧', page: 'tickets' as Page },
    { label: 'Historique', icon: '◷', page: 'reservations' as Page },
    { label: 'Itinéraires', icon: '⌖', page: 'itineraries' as Page },
    { label: 'Événements', icon: '▣', page: 'events' as Page },
    { label: 'Annuaire', icon: '♙', page: 'home' as Page },
    { label: 'Avis et notes', icon: '☆', page: 'reservations' as Page },
    { label: 'Mon espace', icon: '♙', page: 'reservations' as Page },
    { label: 'Administration', icon: '⚙', page: 'admin' as Page },
  ];

  page: Page = 'home';
  visits: Visit[] = [];
  selected: Visit | null = null;
  confirmed: Booking | null = null;
  reservations: Booking[] = [];
  interests: InterestOption[] = [];
  selectedInterests: string[] = [];
  itineraryStartDate = this.today;
  itineraryDays = 3;
  itineraryTravelers = 2;
  itineraryBudget = 60000;
  itinerary: Itinerary | null = null;
  savedItineraries: Itinerary[] = [];
  itineraryLoading = false;
  itineraryError = '';
  events: TourismEvent[] = [];
  eventQuery = '';
  eventType = '';
  eventLocation = '';
  eventDate = '';
  eventFrom = '';
  eventTo = '';
  eventLoading = false;
  eventError = '';
  tickets: Ticket[] = [];
  ticketsLoading = false;
  ticketMessage = '';
  surveyQuestions: SurveyQuestion[] = [];
  surveyAnswers: Record<number, string> = {};
  surveyReference = '';
  surveyLoading = false;
  surveyError = '';
  surveySubmitted = false;
  adminToken = '';
  adminError = '';
  adminMessage = '';
  adminLoaded = false;
  adminBookings: Booking[] = [];
  adminQuestions: SurveyQuestion[] = [];
  adminStatistics: SatisfactionSummary[] = [];
  adminStatsVisitId = '';
  adminStatsFrom = '';
  adminStatsTo = '';
  adminEvent = { title: '', description: '', type: 'Festival', location: '', startsAt: '', endsAt: '', practicalInfo: '', imageUrl: '' };
  adminQuestion = { prompt: '', type: 'SCALE' as SurveyQuestion['type'], options: '', displayOrder: 10 };
  query = '';
  category = '';
  categories: string[] = [];
  bookingDate = '';
  travelers = 2;
  customerName = '';
  email = '';
  step = 1;
  loading = false;
  submitting = false;
  error = '';
  reservationError = '';

  ngOnInit(): void {
    const ticketReference = new URLSearchParams(window.location.search).get('ticketRef');
    if (ticketReference) {
      this.rememberReference(ticketReference);
      this.loadTickets();
      return;
    }
    this.search();
  }

  search(): void {
    this.loading = true;
    this.error = '';
    this.reservationApi.searchVisits(this.query, this.category, this.bookingDate).subscribe({
      next: (rows) => {
        this.visits = rows;
        this.categories = [...new Set(rows.map((visit) => visit.category))];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Le catalogue est momentanément indisponible. Vérifiez que l’API et la base de données sont démarrées.';
      },
    });
  }

  showHome(): void {
    this.page = 'home';
    this.error = '';
    this.search();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  openDetail(visit: Visit): void {
    this.selected = visit;
    this.page = 'detail';
    this.bookingDate = '';
    this.error = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  beginBooking(visit: Visit | null = this.selected): void {
    if (!visit) return;
    this.selected = visit;
    this.page = 'booking';
    this.step = 1;
    this.error = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  nextStep(): void {
    if (this.step === 1) this.step = 2;
    else if (this.step === 2 && this.bookingDate && this.customerName.trim() && this.email.trim()) this.step = 3;
  }

  submitBooking(): void {
    if (!this.selected || !this.bookingDate || !this.customerName.trim() || !this.email.trim()) return;
    this.submitting = true;
    this.error = '';
    this.reservationApi.createBooking({
      visitId: this.selected.id,
      customerName: this.customerName.trim(),
      email: this.email.trim(),
      visitDate: this.bookingDate,
      travelers: this.travelers,
    }).subscribe({
      next: (booking) => {
        this.rememberReference(booking.reference);
        this.confirmed = {
          ...booking,
          visitTitle: booking.visitTitle || this.selected?.title || '',
          location: booking.location || this.selected?.location || '',
          image: booking.image || this.selected?.image || '',
          visitDate: booking.visitDate || this.bookingDate,
          travelers: booking.travelers || this.travelers,
          total: booking.total ?? (this.selected?.price || 0) * this.travelers,
          status: booking.status || 'PENDING',
        };
        this.page = 'confirmation';
        this.submitting = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (response) => {
        this.submitting = false;
        this.error = response.status === 409
          ? 'La capacité disponible ne suffit plus pour cette date. Choisissez un autre jour ou réduisez le nombre de participants.'
          : response.status === 400
            ? 'Certaines informations sont invalides. Vérifiez la date, le nombre de participants et votre adresse e-mail.'
            : 'La réservation n’a pas pu être enregistrée. Réessayez dans un instant.';
      },
    });
  }

  loadReservations(): void {
    this.page = 'reservations';
    this.reservationError = '';
    this.reservations = [];
    // The backend currently has no authenticated owner identity. Do not call its
    // unscoped collection endpoint: it exposes every visitor's personal data.
    const references = this.readReferences();
    if (!references.length) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    references.forEach((reference) => {
      this.reservationApi.getBooking(reference).subscribe({
        next: (booking) => this.reservations = [...this.reservations, booking],
        error: () => this.reservationError = 'Certaines réservations enregistrées sur cet appareil ne sont plus accessibles.',
      });
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  openNavigation(page: Page): void {
    if (page === 'reservations') this.loadReservations();
    else if (page === 'tickets') this.loadTickets();
    else if (page === 'itineraries') this.openItineraryPlanner();
    else if (page === 'events') { this.page = 'events'; this.searchEvents(); }
    else if (page === 'admin') { this.page = 'admin'; this.adminError = ''; this.adminMessage = ''; }
    else this.showHome();
  }

  loadAdmin(): void {
    if (!this.adminToken.trim()) { this.adminError = 'Saisissez le jeton administrateur configuré sur le backend.'; return; }
    this.adminError = '';
    this.adminMessage = '';
    forkJoin({
      bookings: this.reservationApi.adminBookings(this.adminToken.trim()),
      questions: this.reservationApi.adminQuestions(this.adminToken.trim()),
      statistics: this.reservationApi.satisfactionStatistics(this.adminToken.trim(), this.adminStatsVisitId, this.adminStatsFrom, this.adminStatsTo),
    }).subscribe({
      next: (result) => { this.adminBookings = result.bookings; this.adminQuestions = result.questions; this.adminStatistics = result.statistics; this.adminLoaded = true; },
      error: (response) => { this.adminLoaded = false; this.adminError = response.status === 403 ? 'Jeton administrateur invalide.' : 'Administration non configurée ou indisponible.'; },
    });
  }

  refreshAdminStatistics(): void {
    this.reservationApi.satisfactionStatistics(this.adminToken.trim(), this.adminStatsVisitId, this.adminStatsFrom, this.adminStatsTo).subscribe({
      next: (results) => this.adminStatistics = results,
      error: () => this.adminError = 'Impossible de charger les indicateurs pour ces filtres.',
    });
  }

  confirmAdminBooking(booking: Booking): void {
    this.reservationApi.confirmBooking(this.adminToken.trim(), booking.reference).subscribe({
      next: () => { this.adminBookings = this.adminBookings.filter((item) => item.reference !== booking.reference); this.adminMessage = 'Réservation confirmée : billet créé, courriel placé dans la file d’envoi.'; },
      error: (response) => this.adminError = response.status === 409 ? 'Cette demande a déjà changé de statut.' : 'Confirmation impossible.',
    });
  }

  createAdminEvent(): void {
    this.reservationApi.createEvent(this.adminToken.trim(), this.adminEvent).subscribe({
      next: () => { this.adminEvent = { title: '', description: '', type: 'Festival', location: '', startsAt: '', endsAt: '', practicalInfo: '', imageUrl: '' }; this.adminMessage = 'Événement publié dans le catalogue.'; },
      error: () => this.adminError = 'Vérifiez les champs et les dates de l’événement.',
    });
  }

  createAdminQuestion(): void {
    this.reservationApi.createSurveyQuestion(this.adminToken.trim(), this.adminQuestion).subscribe({
      next: (question) => { this.adminQuestions = [...this.adminQuestions, question]; this.adminQuestion = { prompt: '', type: 'SCALE', options: '', displayOrder: this.adminQuestions.length + 1 }; this.adminMessage = 'Question ajoutée au questionnaire.'; },
      error: () => this.adminError = 'La question n’a pas pu être ajoutée. Les choix doivent être séparés par |.',
    });
  }

  toggleAdminQuestion(question: SurveyQuestion): void {
    this.reservationApi.setQuestionActive(this.adminToken.trim(), question.id, !question.active).subscribe({
      next: () => question.active = !question.active,
      error: () => this.adminError = 'Impossible de modifier cette question.',
    });
  }

  openItineraryPlanner(): void {
    this.page = 'itineraries';
    this.itineraryError = '';
    if (!this.interests.length) this.reservationApi.getInterests().subscribe({
      next: (rows) => this.interests = rows,
      error: () => this.itineraryError = 'Les centres d’intérêt sont momentanément indisponibles.',
    });
    this.savedItineraries = [];
    this.readItineraryReferences().forEach((reference) => this.reservationApi.getItinerary(reference).subscribe({
      next: (saved) => this.savedItineraries = [...this.savedItineraries, saved],
    }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  toggleInterest(code: string): void {
    this.selectedInterests = this.selectedInterests.includes(code)
      ? this.selectedInterests.filter((item) => item !== code)
      : [...this.selectedInterests, code];
  }

  generateItinerary(): void {
    if (!this.selectedInterests.length) { this.itineraryError = 'Sélectionnez au moins un centre d’intérêt.'; return; }
    this.itineraryLoading = true;
    this.itineraryError = '';
    this.reservationApi.createItinerary({ interests: this.selectedInterests, startDate: this.itineraryStartDate,
      durationDays: this.itineraryDays, travelers: this.itineraryTravelers, budget: this.itineraryBudget }).subscribe({
      next: (result) => { this.itinerary = result; this.rememberItinerary(result.reference); this.savedItineraries = [result, ...this.savedItineraries.filter((item) => item.reference !== result.reference)]; this.itineraryLoading = false; },
      error: () => { this.itineraryError = 'Impossible de générer ce parcours. Vérifiez vos dates, votre budget et réessayez.'; this.itineraryLoading = false; },
    });
  }

  rememberItinerary(reference: string): void {
    const references = this.readItineraryReferences();
    if (!references.includes(reference)) localStorage.setItem('casamance.itineraryReferences', JSON.stringify([...references, reference]));
  }

  private readItineraryReferences(): string[] {
    try {
      const value: unknown = JSON.parse(localStorage.getItem('casamance.itineraryReferences') || '[]');
      return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
    } catch { return []; }
  }

  openSavedItinerary(itinerary: Itinerary): void {
    this.itinerary = itinerary;
    this.selectedInterests = [...itinerary.interests];
    this.itineraryStartDate = itinerary.startDate;
    this.itineraryDays = itinerary.durationDays;
    this.itineraryTravelers = itinerary.travelers;
    this.itineraryBudget = itinerary.budget;
  }

  searchEvents(): void {
    this.eventLoading = true;
    this.eventError = '';
    this.reservationApi.searchEvents({ type: this.eventType, date: this.eventDate, from: this.eventFrom,
      to: this.eventTo, location: this.eventLocation, q: this.eventQuery }).subscribe({
      next: (rows) => { this.events = rows; this.eventLoading = false; },
      error: () => { this.eventError = 'La recherche d’événements est momentanément indisponible.'; this.eventLoading = false; },
    });
  }

  loadTickets(): void {
    this.page = 'tickets';
    this.tickets = [];
    this.ticketMessage = '';
    this.ticketsLoading = true;
    const references = this.readReferences();
    if (!references.length) { this.ticketsLoading = false; this.ticketMessage = 'Aucune réservation enregistrée sur cet appareil.'; return; }
    let pending = references.length;
    const finished = (): void => { pending--; if (pending === 0) { this.ticketsLoading = false; if (!this.tickets.length) this.ticketMessage = 'Aucun billet émis : une réservation doit être confirmée par le prestataire.'; } };
    references.forEach((reference) => this.reservationApi.getBooking(reference).subscribe({
      next: (booking) => {
        if (booking.status !== 'CONFIRMED' && booking.status !== 'COMPLETED') { finished(); return; }
        this.reservationApi.getTicket(reference).subscribe({
          next: (ticket) => { this.tickets = [...this.tickets, ticket]; finished(); },
          error: () => finished(),
        });
      },
      error: () => finished(),
    }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  openSurvey(booking: Booking): void {
    this.page = 'survey';
    this.surveyReference = booking.reference;
    this.surveyQuestions = [];
    this.surveyAnswers = {};
    this.surveyError = '';
    this.surveySubmitted = false;
    this.surveyLoading = true;
    this.reservationApi.getSurveyQuestions(booking.reference).subscribe({
      next: (questions) => { this.surveyQuestions = questions; this.surveyLoading = false; },
      error: (response) => { this.surveyError = response.status === 409 ? 'L’enquête est accessible après la visite, une fois la réservation terminée.' : 'Impossible de charger le questionnaire.'; this.surveyLoading = false; },
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  submitSurvey(): void {
    const answers = this.surveyQuestions.map((question) => ({ questionId: question.id, value: this.surveyAnswers[question.id] || '' }));
    this.reservationApi.submitSurvey(this.surveyReference, answers).subscribe({
      next: () => { this.surveySubmitted = true; this.surveyError = ''; },
      error: (response) => { this.surveyError = response.status === 409 ? 'Cette enquête a déjà été envoyée ou la visite n’est pas terminée.' : 'Vérifiez toutes vos réponses puis réessayez.'; },
    });
  }

  cancelReservation(booking: Booking): void {
    if (!window.confirm(`Annuler la réservation ${booking.reference} ?`)) return;
    this.reservationApi.cancelBooking(booking.reference).subscribe({
      next: (updated) => this.reservations = this.reservations.map((item) => item.reference === updated.reference ? updated : item),
      error: (response) => this.reservationError = response.status === 409
        ? 'Cette réservation ne peut plus être annulée selon les conditions applicables.'
        : 'L’annulation n’a pas pu être enregistrée.',
    });
  }

  printTicket(): void { window.print(); }

  stopsForDay(day: number) { return this.itinerary?.stops.filter((stop) => stop.dayNumber === day) || []; }
  itineraryDayNumbers(): number[] { return Array.from({ length: this.itinerary?.durationDays || 0 }, (_, index) => index + 1); }
  itineraryDate(day: number): Date {
    const date = new Date(`${this.itinerary?.startDate || this.today}T00:00:00`);
    date.setDate(date.getDate() + day - 1);
    return date;
  }
  setSurveyScore(questionId: number, score: number): void { this.surveyAnswers[questionId] = String(score); }

  bookItineraryStop(visitId: number): void {
    this.reservationApi.getVisit(visitId).subscribe({
      next: (visit) => this.beginBooking(visit),
      error: () => this.itineraryError = 'Cette activité n’est plus disponible dans le catalogue.',
    });
  }

  rememberReference(reference: string | undefined): void {
    if (!reference) return;
    const refs = this.readReferences();
    if (!refs.includes(reference)) localStorage.setItem('casamance.bookingReferences', JSON.stringify([...refs, reference]));
  }

  private readReferences(): string[] {
    try {
      const value: unknown = JSON.parse(localStorage.getItem('casamance.bookingReferences') || '[]');
      return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
    } catch {
      return [];
    }
  }

  imageUrl(visit: Visit | null): string {
    return this.imageUrls[visit?.image || ''] || this.imageUrls['river'];
  }

  imageForKey(image: string | undefined): string {
    return this.imageUrls[image || ''] || this.imageUrls['river'];
  }

  private localDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  statusLabel(status: string): string {
    return status === 'CONFIRMED' ? 'Confirmée' : status === 'CANCELLED' ? 'Annulée' : status === 'COMPLETED' ? 'Terminée' : 'En attente';
  }

  isNavigationActive(label: string): boolean {
    if (label === 'Itinéraires') return this.page === 'itineraries';
    if (label === 'Événements') return this.page === 'events';
    if (label === 'Mes billets') return this.page === 'tickets';
    if (label === 'Administration') return this.page === 'admin';
    if (label === 'Mes réservations' || label === 'Historique' || label === 'Mon espace' || label === 'Avis et notes') return this.page === 'reservations';
    if (label === 'Réserver') return ['home', 'detail', 'booking', 'confirmation'].includes(this.page);
    return label === 'Accueil' && this.page === 'home';
  }

  get total(): number {
    return (this.selected?.price || 0) * this.travelers;
  }
}
