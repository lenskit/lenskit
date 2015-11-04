#!/bin/sh

if [ -n "$encrypted_63db7f4dcbd2_key" ]; then
    openssl aes-256-cbc -K $encrypted_63db7f4dcbd2_key -iv $encrypted_63db7f4dcbd2_iv \
        -in etc/web-deploy-key.rsa.enc -out etc/web-deploy-key.rsa -d
    install -m 0600 etc/web-deploy-key.rsa "$HOME/.ssh/id_rsa"
fi
