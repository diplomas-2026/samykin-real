import { Link } from 'react-router-dom';

export function LandingPage() {
  return (
    <div className="landing">
      <div className="hero-card">
        <div className="eyebrow">Платформа для ИП Самыкин</div>
        <h1>Распределение и выдача денежных выплат без ручной рутины</h1>
        <p>
          Система помогает управлять выплатами сотрудникам, отслеживать статусы выдачи,
          формировать Excel-отчеты и использовать AI для подготовки аккуратных комментариев.
        </p>
        <div className="hero-actions">
          <Link className="primary-button" to="/login">
            Войти в систему
          </Link>
        </div>
      </div>
      <section className="feature-grid">
        <article className="feature-card">
          <h3>Контроль статусов</h3>
          <p>Создание, подготовка, выдача и отмена выплат в одном понятном интерфейсе.</p>
        </article>
        <article className="feature-card">
          <h3>AI-помощник бухгалтера</h3>
          <p>Генерация комментариев к выплатам через GigaChat со скрытым системным prompt.</p>
        </article>
        <article className="feature-card">
          <h3>Excel-выгрузки</h3>
          <p>Отчеты за период, по сотрудникам, по статусам и сводка по суммам.</p>
        </article>
      </section>
    </div>
  );
}
