# -*- coding:utf-8 -*-

from flask_script import Manager

from ens.app import app
from commands.seed_registry import SeedFirestore


manager = Manager(app)
manager.add_command('seed_firestore', SeedFirestore)



if __name__ == '__main__':
    manager.run()
