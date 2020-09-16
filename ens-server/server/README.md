COVID Guard-F Diagnosis Key Server
----------------------------------------


The Diagnosis Key Server for the covid-guard F application.

The server is hosted on google app engine.
Hostname: https://covidgaurd-285412.ts.r.appspot.com/

# Local Setup

## Virtual Environment
To install virtualenvwrapper with your default shell - https://itnext.io/virtualenv-with-virtualenvwrapper-on-ubuntu-18-04-goran-aviani-d7b712d906d5

1. Create a python3 virtual env  - `mkvirtualenv --python=/use/bin/python3 ens`
2. `workon ens` to enter the virtualenv environment
3. Install the Google Cloud sdk
4. Add Project Absolute Path for ex: `$HOME/projects/projects/CovidGuard-F/ens-server/server` to PYTHONPATH in `$HOME/.virtualenvs/covid/postactivate`
    The command is `export PYTHON_PATH=$HOME/projects/CovidGuard-F/ens-server/server:$PYTHONPATH`

Sample PostActivate file looks like this - 
```sh
#!/usr/bin/zsh
# This hook is sourced after this virtualenv is activated.

cd $HOME/projects/CovidGuard-F/server
export PYTHON_PATH=$HOME/projects/CovidGuard-F/server:$PYTHONPATH
```

## Installation
1. Run `pip install -r requirements.txt`
2. Run `sudo apt-get install google-cloud-sdk-app-engine-python` to install the
   app engine emulator
3. Run `APP_MODE=dev python main.py` to run the server on `localhost:8080`

## Testing (Unit and Integration tests)
1. Run `pip install -r requirements-test.txt`
2. Run `pytest tests`

Firestore test collections will be used for testing the app. Separate folders
in Firestore (for test/dev/prod) are being setup.

## Testing on app-engine emulator
1. Run `dev_appserver.py app.yaml --env_var APP_MODE=dev`

# Deployment to app engine

1. Run `gcloud app deploy --no-promote`
2. Test your version
3. Deploy the version as latest in app engine console
