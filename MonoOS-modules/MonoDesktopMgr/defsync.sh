#!/bin/bash

cp -v ./def/*.service ../../sys/kernel/services/
cp -v ./def/*.service.disabled /../../sys/kernel/services/
echo "Done!"
