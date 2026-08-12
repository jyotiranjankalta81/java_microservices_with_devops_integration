import { useAuth } from '../hooks/useAuth';

export default function ProfilePage() {
  const { user } = useAuth();
  return (
    <div className="card">
      <h2>Profile</h2>
      <p><strong>Email:</strong> {user?.email}</p>
      <p><strong>Name:</strong> {user?.fullName}</p>
      <p><strong>Roles:</strong> {(user?.roles ?? []).join(', ')}</p>
    </div>
  );
}
