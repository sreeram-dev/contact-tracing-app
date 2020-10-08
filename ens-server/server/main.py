import os

from ens.app import app
from ens import routes

## Strict https
from flask_talisman import Talisman

SELF =  '\'self\''
CSP = {
    'default-src': [
        SELF,
        'https://fonts.googleapis.com',
        'https://ens-server.ts.r.appspot.com',
    ]
}
Talisman(app, content_security_policy=CSP)


if __name__ == '__main__':
    if not app.config['DEBUG']:
        PORT = int(os.environ.get('PORT', 8081))
        app.run(host='0.0.0.0', port=PORT)
    elif app.config['DEBUG']:
        app.run(host='127.0.0.1', port=8081)
