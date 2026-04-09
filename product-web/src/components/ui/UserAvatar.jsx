export function UserAvatar({ user, size = 'md' }) {
  const initials = (user?.fullName || 'Сотрудник')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'С';

  if (user?.photoUrl) {
    return (
      <img
        className={`user-avatar user-avatar--${size}`}
        src={user.photoUrl}
        alt={`Фото ${user.fullName}`}
      />
    );
  }

  return (
    <div className={`user-avatar user-avatar--${size} user-avatar--placeholder`} aria-hidden="true">
      {initials}
    </div>
  );
}
