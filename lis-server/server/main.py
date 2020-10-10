import os

from lis.app import app
from lis import routes

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
        PORT = int(os.environ.get('PORT', 8082))
        app.run(host='0.0.0.0', port=PORT)
    elif app.config['DEBUG']:
        app.run(host='127.0.0.1', port=8082)
