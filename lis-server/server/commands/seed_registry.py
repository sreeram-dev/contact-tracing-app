# -*- coding:utf-8 -*-
"""Seed registry to seed the initial database
"""

# -*- coding:utf-8 -*-


import os
import json

from flask_script import Command

from lis.app import app
from lis.providers.db import FireStoreClient
from lis.constants import TOKEN_COLLECTION
from lis.utils import get_collection_name


class SeedFirestore(Command):
    """Command to initialise firestore collections
       Initialize the following collections -
       1. tokens
       2. tans
       3. rpis
    """

    def run(self):
        app.logger.info('Firestore collections are created automatically, no \
                need to write them')
