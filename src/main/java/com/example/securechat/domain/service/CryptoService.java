package com.example.securechat.domain.service;

import java.util.UUID;

/**
 * Placeholder interface describing where a Signal-capable crypto engine (e.g., libsignal) would be integrated.
 * Implementations should remain client-side for end-to-end encryption; the server must never hold plaintext.
 */
public interface CryptoService {

    /**
     * Client libraries should establish a secure session using identity keys, signed pre-keys, and one-time pre-keys.
     * This method is intentionally left unimplemented on the server to prevent accidental plaintext handling.
     */
    void createSession(UUID userId, UUID peerId);

    /**
     * Encrypt message content on the client. The server never implements this.
     */
    default String encrypt(byte[] plaintext) {
        throw new UnsupportedOperationException("Encryption must be handled client-side using Signal protocol.");
    }

    /**
     * Decrypt message content on the client. The server never implements this.
     */
    default byte[] decrypt(String ciphertext) {
        throw new UnsupportedOperationException("Decryption must be handled client-side using Signal protocol.");
    }
}
