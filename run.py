from flask import Flask, render_template, session
from flask_socketio import SocketIO, emit, join_room, leave_room
import uuid

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app)

@app.route('/')
def index():
	return render_template('index.html')
	
@socketio.on('my event')
def my_event(message):
	print(message['data'])
	
@socketio.on('broadcast')
def do_broadcast(message):
	print(message['data'])
	emit('app broadcast', {'data': message['data']}, broadcast=True)
	
@socketio.on('register')
def my_event(message):
	print('uid=%s' % (message['data']))

@socketio.on('p2p')
def do_p2p_message(message):
	emit(message['id'], {'data': message['data']}, broadcast=True)
	print('to=%s, message=%s' % (message['id'], message['data']))
	
	
#########################

GLOBAL_NUM_USERS = 0

@app.route('/chat')
def chat():
	return render_template('chat.html')

@socketio.on('joined')
def joined(message):
	"""Sent by clients when they enter a room.
	A _joined message is broadcast to all people in the room."""
	global GLOBAL_NUM_USERS
	GLOBAL_NUM_USERS = GLOBAL_NUM_USERS + 1
	print(message)
	session['name'] = message['name']
	session['room'] = message['room']
	room = session.get('room')
	join_room(room)
	print('%s : joined' % session)
	emit('_joined', {'user_name': session.get('name'), 'num_users' : GLOBAL_NUM_USERS}, room=room)

@socketio.on('message')
def message(message):
	"""Sent by a client when the user entered a new message.
	The _message is sent to all people in the room."""
	room = session.get('room')
	print('%s : message : %s' % (session, message['message']))
	emit('_message', {'user_name': session.get('name'), 'message' : message['message']}, room=room, include_self=False)

@socketio.on('left')
def left(message):
	"""Sent by clients when they leave a room.
	A _left message is broadcast to all people in the room."""
	global GLOBAL_NUM_USERS
	GLOBAL_NUM_USERS = GLOBAL_NUM_USERS - 1
	room = session.get('room')
	leave_room(room)
	print('%s : left' % session)
	emit('_left', {'user_name': session.get('name'), 'num_users' : GLOBAL_NUM_USERS}, room=room)

if __name__ == '__main__':
	socketio.run(app, host='0.0.0.0', port=5000)