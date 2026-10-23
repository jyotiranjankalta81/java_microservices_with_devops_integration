import React, { useEffect, useState } from 'react';
import { evidencingApi, UserScopeResponse } from '../../api/evidencingApi';

interface RoleSwitcherProps {
  onUserChanged: (user: UserScopeResponse) => void;
}

export const PRESET_USERS = [
  {
    userId: '11111111-1111-1111-1111-111111111111',
    name: 'Sarah Jenkins',
    role: 'TEACHER',
    desc: 'Grade 4A Teacher (Oakridge North - Post Creator)',
  },
  {
    userId: '22222222-2222-2222-2222-222222222222',
    name: 'Emily Clark',
    role: 'TEACHER',
    desc: 'Grade 4A Teacher (Oakridge North - Tagged in Private Post)',
  },
  {
    userId: '33333333-3333-3333-3333-333333333333',
    name: 'Mark Wilson',
    role: 'TEACHER',
    desc: 'Grade 5B Teacher (Oakridge North - Untagged in Private Post)',
  },
  {
    userId: '44444444-4444-4444-4444-444444444444',
    name: 'Robert Davis',
    role: 'BRANCH_ADMIN',
    desc: 'Branch Admin (Oakridge North - Untagged in Private Post)',
  },
  {
    userId: '55555555-5555-5555-5555-555555555555',
    name: 'Jennifer Adams',
    role: 'SCHOOL_ADMIN',
    desc: 'School Admin (Oakridge Central)',
  },
  {
    userId: '66666666-6666-6666-6666-666666666666',
    name: 'David Miller',
    role: 'TEACHER',
    desc: 'Grade 3A Teacher (Oakridge South - Different Branch)',
  },
  {
    userId: '77777777-7777-7777-7777-777777777777',
    name: 'Mary Smith',
    role: 'NANNY',
    desc: 'Nanny (Oakridge North - DENY BY DEFAULT)',
  },
  {
    userId: '88888888-8888-8888-8888-888888888888',
    name: 'John Brown',
    role: 'PEON',
    desc: 'Peon (Oakridge North - DENY BY DEFAULT)',
  },
  {
    userId: '99999999-9999-9999-9999-999999999999',
    name: 'James Garcia',
    role: 'DRIVER',
    desc: 'Driver (Oakridge North - DENY BY DEFAULT)',
  },
  {
    userId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    name: 'Lisa Martinez',
    role: 'ADMISSION_COORDINATOR',
    desc: 'Admission Coordinator (Oakridge North)',
  },
  {
    userId: 'bbbb0001-0000-0000-0000-000000000001',
    name: 'Michael Anderson',
    role: 'PARENT',
    desc: 'Parent (Grade 4A, Oakridge North - Tagged in Private Post 12)',
  },
  {
    userId: 'bbbb0002-0000-0000-0000-000000000002',
    name: 'Sophia Taylor',
    role: 'PARENT',
    desc: 'Parent (Grade 5B, Oakridge North - Untagged in Private Post 12)',
  },
  {
    userId: 'bbbb0003-0000-0000-0000-000000000003',
    name: 'Lucas Martinez',
    role: 'PARENT',
    desc: 'Parent (Grade 4A, Oakridge North - Tagged in Private Post 12)',
  },
];

export default function RoleSwitcher({ onUserChanged }: RoleSwitcherProps) {
  const [currentUser, setCurrentUser] = useState<UserScopeResponse | null>(null);

  useEffect(() => {
    // Check saved simulated user or default to Sarah Jenkins
    const saved = localStorage.getItem('evidencing_simulated_user');
    if (!saved) {
      const defaultUser = {
        userId: PRESET_USERS[0].userId,
        role: PRESET_USERS[0].role,
      };
      localStorage.setItem('evidencing_simulated_user', JSON.stringify(defaultUser));
    }
    loadCurrentScope();
  }, []);

  const loadCurrentScope = async () => {
    try {
      const scope = await evidencingApi.getCurrentUserScope();
      setCurrentUser(scope);
      onUserChanged(scope);
    } catch (e) {
      console.error('Failed to load user scope', e);
    }
  };

  const handleSelectUser = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedId = e.target.value;
    const preset = PRESET_USERS.find((u) => u.userId === selectedId);
    if (!preset) return;

    localStorage.setItem(
      'evidencing_simulated_user',
      JSON.stringify({ userId: preset.userId, role: preset.role })
    );

    loadCurrentScope();
  };

  const selectedUserId = currentUser?.userId || PRESET_USERS[0].userId;
  const hasViewAccess = currentUser?.permissions?.includes('EVIDENCING_VIEW');

  return (
    <div className="persona-bar">
      <div className="persona-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span className="persona-badge">Staff Role Simulator</span>
          <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>
            Switch roles to test permissions, school/branch scope, and private post visibility
          </span>
        </div>
        <div className="persona-selector">
          <select value={selectedUserId} onChange={handleSelectUser}>
            {PRESET_USERS.map((u) => (
              <option key={u.userId} value={u.userId}>
                {u.name} — {u.role} ({u.desc})
              </option>
            ))}
          </select>
        </div>
      </div>

      {currentUser && (
        <div className="persona-info">
          <div className="persona-user-item">
            {currentUser.avatarUrl && (
              <img
                src={currentUser.avatarUrl}
                alt={currentUser.displayName}
                className="persona-avatar"
              />
            )}
            <div>
              <strong style={{ color: '#fff' }}>{currentUser.displayName}</strong>{' '}
              <span style={{ color: '#38bdf8' }}>({currentUser.role})</span>
            </div>
          </div>

          <div>
            🏫 <strong>{currentUser.schoolName || 'School'}</strong> • 📍{' '}
            <strong>{currentUser.branchName || 'Branch'}</strong>
            {currentUser.className && (
              <span>
                {' '}
                • 🎓 <strong>{currentUser.className}</strong>
              </span>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Permissions:</span>
            <div className="permission-tags">
              {currentUser.permissions && currentUser.permissions.length > 0 ? (
                currentUser.permissions.map((p) => (
                  <span key={p} className="perm-pill">
                    {p.replace('EVIDENCING_', '')}
                  </span>
                ))
              ) : (
                <span className="perm-pill none">DENY-BY-DEFAULT (NO ACCESS)</span>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
