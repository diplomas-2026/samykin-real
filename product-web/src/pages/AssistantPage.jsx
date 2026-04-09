import { useEffect, useMemo, useState } from 'react';
import { aiApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { getApiErrorMessage } from '../utils/api';
import { formatDateTime } from '../utils/formatters';

export function AssistantPage() {
  const [usage, setUsage] = useState(null);
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: 'Здравствуйте. Я могу ответить на вопросы только по вашим выплатам: по суммам, датам, статусам и комментариям.',
    },
  ]);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    aiApi.getUsage()
      .then(setUsage)
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, 'Не удалось загрузить лимиты AI'));
      });
  }, []);

  const canSend = draft.trim().length > 0 && !loading;
  const quickQuestions = useMemo(() => ([
    'Какие у меня выплаты сейчас в статусе Подготовлена?',
    'Какая у меня ближайшая выплата по дате?',
    'Объясни, какие выплаты у меня уже выданы.',
  ]), []);

  const sendMessage = async (messageText) => {
    const trimmed = messageText.trim();
    if (!trimmed) {
      return;
    }

    const nextUserMessage = { role: 'user', content: trimmed };
    const nextMessages = [...messages, nextUserMessage];
    setMessages(nextMessages);
    setDraft('');
    setLoading(true);
    setError('');

    try {
      const result = await aiApi.chat({
        message: trimmed,
        history: messages
          .filter((message) => message.role === 'user' || message.role === 'assistant')
          .slice(-8)
          .map((message) => ({ role: message.role, content: message.content })),
      });

      setMessages((current) => [...current, { role: 'assistant', content: result.reply }]);
      setUsage((current) => current ? { ...current, usedTokens: current.usedTokens + result.usedTokens, remainingTokens: result.remainingTokens } : current);
    } catch (requestError) {
      setMessages((current) => current.filter((message, index) => !(index === current.length - 1 && message.role === 'user' && message.content === trimmed)));
      setDraft(trimmed);
      setError(getApiErrorMessage(requestError, 'Не удалось получить ответ AI-помощника'));
    } finally {
      setLoading(false);
    }
  };

  if (!usage) {
    if (error) {
      return (
        <div className="stack">
          <PageHeader
            eyebrow="AI-помощник"
            title="Чат по вашим выплатам"
            description="Сотрудник может задавать вопросы только по своим выплатам и их статусам."
          />
          <ErrorBanner message={error} />
        </div>
      );
    }
    return <Loader text="Подготовка AI-помощника..." />;
  }

  return (
    <div className="stack">
      <PageHeader
        eyebrow="AI-помощник"
        title="Чат по вашим выплатам"
        description="Сотрудник может задавать вопросы только по своим выплатам и их статусам."
      />
      <ErrorBanner message={error} />

      <section className="dashboard-grid">
        <div className="panel stack-sm">
          <h3>Лимиты AI</h3>
          <div className="usage-list">
            <div><span>Потрачено</span><strong>{usage.usedTokens}</strong></div>
            <div><span>Осталось</span><strong>{usage.remainingTokens}</strong></div>
            <div><span>Сброс лимита</span><strong>{formatDateTime(usage.resetsAt)}</strong></div>
          </div>
        </div>

        <div className="panel stack-sm">
          <h3>Подсказки для вопросов</h3>
          <div className="assistant-quick-list">
            {quickQuestions.map((question) => (
              <button key={question} type="button" className="assistant-quick-button" disabled={loading} onClick={() => sendMessage(question)}>
                {question}
              </button>
            ))}
          </div>
        </div>
      </section>

      <div className="panel assistant-panel">
        <div className="assistant-messages">
          {messages.map((message, index) => (
            <div key={`${message.role}-${index}`} className={`assistant-message assistant-message--${message.role}`}>
              <span className="assistant-message__role">{message.role === 'assistant' ? 'AI-помощник' : 'Вы'}</span>
              <p>{message.content}</p>
            </div>
          ))}
          {loading ? (
            <div className="assistant-message assistant-message--assistant">
              <span className="assistant-message__role">AI-помощник</span>
              <p>Подбираю ответ по вашим выплатам...</p>
            </div>
          ) : null}
        </div>

        <form
          className="assistant-form"
          onSubmit={(event) => {
            event.preventDefault();
            sendMessage(draft);
          }}
        >
          <textarea
            rows="3"
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            placeholder="Например: какие выплаты у меня еще не выданы?"
          />
          <div className="button-row">
            <button className="primary-button" type="submit" disabled={!canSend}>
              {loading ? 'Отправляем...' : 'Отправить вопрос'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
