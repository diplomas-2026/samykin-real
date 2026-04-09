import dayjs from 'dayjs';
import 'dayjs/locale/ru';

dayjs.locale('ru');

export const roleLabels = {
  ADMIN: 'Администратор',
  ACCOUNTANT: 'Бухгалтер',
  EMPLOYEE: 'Сотрудник',
};

export const statusLabels = {
  CREATED: 'Создана',
  PREPARED: 'Подготовлена',
  PAID: 'Выдана',
  CANCELLED: 'Отменена',
};

export function formatCurrency(value) {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

export function formatDate(value) {
  return value ? dayjs(value).format('DD.MM.YYYY') : '—';
}

export function formatDateTime(value) {
  return value ? dayjs(value).format('DD.MM.YYYY HH:mm') : '—';
}

export function downloadBlob(blob, fileName) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  window.URL.revokeObjectURL(url);
}
