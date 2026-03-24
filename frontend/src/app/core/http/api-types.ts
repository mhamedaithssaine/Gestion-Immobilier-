export interface ApiRetour<T> {
  status: boolean;
  message: string;
  timestamp: string;
  data: T;
}
