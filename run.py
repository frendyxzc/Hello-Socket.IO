from flask import Flask, render_template
from flask_socketio import SocketIO

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app)

@app.route('/')
def index():
	return render_template('index.html')
	
@socketio.on('my event')
def my_event(message):
	print(message['data'])

if __name__ == '__main__':
	socketio.run(app, host='0.0.0.0', port=5000)