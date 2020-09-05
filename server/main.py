import os

from covid.app import app
from covid import routes

## Strict https
from flask_talisman import Talisman
SELF =  '\'self\''
CSP = {
    'default-src': [
        SELF,
        'https://fonts.googleapis.com',
        'https://covidgaurd-285412.ts.r.appspot.com/'
    ]
}
Talisman(app, content_security_policy=CSP)


import sqreen
sqreen.start()



if __name__ == '__main__':
    if not app.config['DEBUG']:
        PORT = int(os.environ.get('PORT', 8080))
        app.run(host='0.0.0.0', port=PORT)
    elif app.config['DEBUG']:
        app.run(host='127.0.0.1', port=8080)
