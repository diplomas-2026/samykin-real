export function getApiErrorMessage(error, fallback = 'Произошла ошибка при обращении к серверу') {
  const details = error?.response?.data?.details;
  if (Array.isArray(details) && details.length > 0) {
    return details[0];
  }

  const message = error?.response?.data?.message || error?.response?.data?.error;
  if (typeof message === 'string' && message.trim()) {
    return message;
  }

  if (typeof error?.message === 'string' && error.message.trim() && error.message !== 'Network Error') {
    return error.message;
  }

  return fallback;
}
