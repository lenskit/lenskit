#!/bin/sh

if [ -n "$encrypted_63db7f4dcbd2_key" ]; then
    openssl aes-256-cbc -K $encrypted_63db7f4dcbd2_key -iv $encrypted_63db7f4dcbd2_iv \
        -in etc/web-deploy-key.rsa.enc -out $HOME/.ssh/id_rsa -d
fi
