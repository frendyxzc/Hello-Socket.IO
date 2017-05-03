from flask import Flask, render_template
from flask_socketio import SocketIO, emit
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

if __name__ == '__main__':
	socketio.run(app, host='0.0.0.0', port=5000)