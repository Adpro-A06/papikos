// WebSocket Chat Client
class ChatWebSocket {
    constructor(chatroomId, currentUserId) {
        this.chatroomId = chatroomId;
        this.currentUserId = currentUserId;
        this.stompClient = null;
        this.connected = false;

        this.connect();
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        // Disable debug logging
        this.stompClient.debug = null;

        const self = this;

        this.stompClient.connect({}, function(frame) {
            console.log('Connected to WebSocket: ' + frame);
            self.connected = true;

            // Subscribe to new messages
            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId, function(message) {
                const newMessage = JSON.parse(message.body);
                self.handleNewMessage(newMessage);
            });

            // Subscribe to message edits
            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/edit', function(message) {
                const editedMessage = JSON.parse(message.body);
                self.handleMessageEdit(editedMessage);
            });

            // Subscribe to message deletions
            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/delete', function(message) {
                const messageId = JSON.parse(message.body);
                self.handleMessageDelete(messageId);
            });

            // Subscribe to reload events (for undo operations)
            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/reload', function(message) {
                self.reloadMessages();
            });

        }, function(error) {
            console.error('WebSocket connection error: ', error);
            self.connected = false;
            // Retry connection after 5 seconds
            setTimeout(() => self.connect(), 5000);
        });
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
            this.connected = false;
        }
        console.log("WebSocket Disconnected");
    }

    sendMessage(content) {
        if (this.connected && this.stompClient) {
            const message = {
                senderId: this.currentUserId,
                content: content
            };

            this.stompClient.send('/app/chat/' + this.chatroomId + '/send', {}, JSON.stringify(message));
        } else {
            console.error('WebSocket not connected');
            // Fallback to REST API
            this.sendMessageViaRest(content);
        }
    }

    handleNewMessage(message) {
        // Add message to chat UI
        this.appendMessageToChat(message);

        // Scroll to bottom
        this.scrollToBottom();

        // Show notification if message is from another user
        if (message.senderId !== this.currentUserId) {
            this.showNotification('New message received');
        }
    }

    handleMessageEdit(message) {
        // Update existing message in UI
        const messageElement = document.querySelector(`[data-message-id="${message.id}"]`);
        if (messageElement) {
            const contentElement = messageElement.querySelector('.message-content');
            if (contentElement) {
                contentElement.textContent = message.content;
                // Add edited indicator
                let editedIndicator = messageElement.querySelector('.edited-indicator');
                if (!editedIndicator) {
                    editedIndicator = document.createElement('span');
                    editedIndicator.className = 'edited-indicator text-muted small';
                    editedIndicator.textContent = ' (edited)';
                    contentElement.appendChild(editedIndicator);
                }
            }
        }
    }

    handleMessageDelete(messageId) {
        // Remove message from UI
        const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
        if (messageElement) {
            messageElement.remove();
        }
    }

    appendMessageToChat(message) {
        const messagesContainer = document.getElementById('messages-container');
        if (!messagesContainer) return;

        // Check if message already exists
        if (document.querySelector(`[data-message-id="${message.id}"]`)) {
            return;
        }

        const messageElement = document.createElement('div');
        messageElement.className = `message ${message.senderId === this.currentUserId ? 'own-message' : 'other-message'}`;
        messageElement.setAttribute('data-message-id', message.id);

        const timestamp = new Date(message.timestamp).toLocaleTimeString();

        messageElement.innerHTML = `
            <div class="message-content">${this.escapeHtml(message.content)}</div>
            <div class="message-meta">
                <small class="text-muted">${timestamp}</small>
                ${message.edited ? '<span class="edited-indicator text-muted small"> (edited)</span>' : ''}
            </div>
        `;

        messagesContainer.appendChild(messageElement);
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('messages-container');
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    showNotification(message) {
        // Simple notification - you can enhance this
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification('Chat', { body: message });
        }
    }

    reloadMessages() {
        // Reload all messages from server
        fetch(`/api/chatrooms/${this.chatroomId}/messages`)
            .then(response => response.json())
            .then(messages => {
                const messagesContainer = document.getElementById('messages-container');
                if (messagesContainer) {
                    messagesContainer.innerHTML = '';
                    messages.reverse().forEach(message => {
                        this.appendMessageToChat(message);
                    });
                    this.scrollToBottom();
                }
            })
            .catch(error => console.error('Error reloading messages:', error));
    }

    sendMessageViaRest(content) {
        // Fallback method using REST API
        fetch(`/api/chatrooms/${this.chatroomId}/messages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                senderId: this.currentUserId,
                content: content
            })
        })
            .then(response => response.json())
            .then(message => {
                // Message will be broadcast via WebSocket from server
                console.log('Message sent via REST API');
            })
            .catch(error => {
                console.error('Error sending message:', error);
            });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize WebSocket when page loads
document.addEventListener('DOMContentLoaded', function() {
    // Get chatroom ID and current user ID from the page
    const chatroomId = document.getElementById('chatroom-id')?.value;
    const currentUserId = document.getElementById('current-user-id')?.value;

    if (chatroomId && currentUserId) {
        window.chatWebSocket = new ChatWebSocket(chatroomId, currentUserId);

        // Handle message form submission
        const messageForm = document.getElementById('message-form');
        const messageInput = document.getElementById('message-input');

        if (messageForm && messageInput) {
            messageForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const content = messageInput.value.trim();
                if (content) {
                    window.chatWebSocket.sendMessage(content);
                    messageInput.value = '';
                }
            });
        }

        // Request notification permission
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }
});

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    if (window.chatWebSocket) {
        window.chatWebSocket.disconnect();
    }
});