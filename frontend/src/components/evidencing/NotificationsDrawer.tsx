import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { evidencingApi, NotificationResponse } from '../../api/evidencingApi';

export default function NotificationsDrawer() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const popoverRef = useRef<HTMLDivElement>(null);

  const fetchNotifications = async () => {
    try {
      const list = await evidencingApi.getNotifications();
      setNotifications(list);
      const count = list.filter((n) => !n.read).length;
      setUnreadCount(count);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 10000); // 10s poll
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const handleClickOutside = (evt: MouseEvent) => {
      if (popoverRef.current && !popoverRef.current.contains(evt.target as Node)) {
        setOpen(false);
      }
    };
    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const handleOpenNotification = async (notif: NotificationResponse) => {
    try {
      if (!notif.read) {
        await evidencingApi.markNotificationRead(notif.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === notif.id ? { ...n, read: true } : n))
        );
        setUnreadCount((c) => Math.max(0, c - 1));
      }
      setOpen(false);
      navigate(`/evidencing/${notif.postId}`);
    } catch (e) {
      console.error(e);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await evidencingApi.markAllNotificationsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={{ position: 'relative' }} ref={popoverRef}>
      <button
        type="button"
        className="btn-secondary"
        style={{
          position: 'relative',
          padding: '0.45rem 0.8rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.4rem',
        }}
        onClick={() => setOpen(!open)}
        title="Audience Notifications"
      >
        <span>🔔</span>
        <span style={{ fontSize: '0.85rem' }}>Notifications</span>
        {unreadCount > 0 && (
          <span
            style={{
              background: '#ef4444',
              color: '#fff',
              borderRadius: '9999px',
              padding: '0.1rem 0.45rem',
              fontSize: '0.75rem',
              fontWeight: 700,
            }}
          >
            {unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="notifications-popover">
          <div className="notif-header">
            <strong>Audience Notifications</strong>
            {unreadCount > 0 && (
              <button
                type="button"
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#2563eb',
                  fontSize: '0.75rem',
                  cursor: 'pointer',
                  fontWeight: 600,
                }}
                onClick={handleMarkAllRead}
              >
                Mark all read
              </button>
            )}
          </div>

          <div className="notif-list">
            {notifications.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#94a3b8', fontSize: '0.85rem' }}>
                No notifications yet.
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`notif-item ${!n.read ? 'unread' : ''}`}
                  onClick={() => handleOpenNotification(n)}
                >
                  <div className="notif-message">
                    <strong>{n.creatorName}</strong>: {n.message}
                  </div>
                  <div className="notif-time">
                    {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} •{' '}
                    {new Date(n.createdAt).toLocaleDateString()}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
