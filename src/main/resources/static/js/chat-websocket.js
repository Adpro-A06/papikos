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

        this.stompClient.debug = null;

        const self = this;

        this.stompClient.connect({}, function(frame) {
            console.log('Connected to WebSocket: ' + frame);
            self.connected = true;

            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId, function(message) {
                const newMessage = JSON.parse(message.body);
                self.handleNewMessage(newMessage);
            });

            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/edit', function(message) {
                const editedMessage = JSON.parse(message.body);
                self.handleMessageEdit(editedMessage);
            });

            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/delete', function(message) {
                const messageId = JSON.parse(message.body);
                self.handleMessageDelete(messageId);
            });

            self.stompClient.subscribe('/topic/chatroom/' + self.chatroomId + '/reload', function(message) {
                self.reloadMessages();
            });

        }, function(error) {
            console.error('WebSocket connection error: ', error);
            self.connected = false;
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
            this.sendMessageViaRest(content);
        }
    }

    handleNewMessage(message) {
        this.appendMessageToChat(message);
        this.scrollToBottom();
        if (message.senderId !== this.currentUserId) {
            this.showNotification('New message received');
        }
    }

    handleMessageEdit(message) {
        const messageElement = document.querySelector(`[data-message-id="${message.id}"]`);
        if (messageElement) {
            const contentElement = messageElement.querySelector('.message-content');
            if (contentElement) {
                contentElement.textContent = message.content;
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
        const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
        if (messageElement) {
            messageElement.remove();
        }
    }

    appendMessageToChat(message) {
        const messagesContainer = document.getElementById('messages-container');
        if (!messagesContainer) return;

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
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification('Chat', { body: message });
        }
    }

    reloadMessages() {
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

document.addEventListener('DOMContentLoaded', function() {
    const chatroomId = document.getElementById('chatroom-id')?.value;
    const currentUserId = document.getElementById('current-user-id')?.value;

    if (chatroomId && currentUserId) {
        window.chatWebSocket = new ChatWebSocket(chatroomId, currentUserId);

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

        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }
});

window.addEventListener('beforeunload', function() {
    if (window.chatWebSocket) {
        window.chatWebSocket.disconnect();
    }
});