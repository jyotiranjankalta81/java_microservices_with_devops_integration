export function useAuth() {
  const token = localStorage.getItem('accessToken');
  const rawUser = localStorage.getItem('user');
  const userData = rawUser ? JSON.parse(rawUser) : null;
  const roles: string[] = Array.isArray(userData?.roles) 
    ? userData.roles 
    : (userData?.role ? [userData.role] : []);
  const isAdmin = roles.some((r: string) => 
    ['ADMIN', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'].includes(r.toUpperCase())
  );

  return {
    token,
    user: userData,
    isAdmin,
    login: (accessToken: string, uData: object) => {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('user', JSON.stringify(uData));
      window.location.href = '/dashboard';
    },
    logout: () => {
      localStorage.clear();
      window.location.href = '/login';
    },
  };
}
