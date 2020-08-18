from covid.app import app

@app.route("/")
def hello_world():
    return "Welcome to covid-guard F verification server"
