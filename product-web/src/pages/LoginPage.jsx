import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PasswordField } from '../components/ui/PasswordField';
import { useAuth } from '../hooks/useAuth';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const user = await login(form);
      navigate(user.role === 'EMPLOYEE' ? '/my-payouts' : '/dashboard');
    } catch (requestError) {
      setError(requestError.response?.data?.details?.[0] || 'Не удалось выполнить вход');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="eyebrow">Вход</div>
        <h1>Добро пожаловать в Samykin Pay</h1>
        <p>Используйте учетную запись, созданную администратором системы.</p>
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            placeholder="ivanov@samykin.local"
          />
        </label>
        <PasswordField
          label="Пароль"
          name="password"
          value={form.password}
          onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
          placeholder="Введите пароль"
        />
        {error ? <div className="error-banner">{error}</div> : null}
        <button className="primary-button" type="submit" disabled={loading}>
          {loading ? 'Выполняется вход...' : 'Войти'}
        </button>
      </form>
    </div>
  );
}
