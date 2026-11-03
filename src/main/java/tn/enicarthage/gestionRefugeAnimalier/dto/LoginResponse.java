package tn.enicarthage.gestionRefugeAnimalier.dto;

/**
 * Reponse de POST /auth/login : le jeton a renvoyer dans l'en-tete
 * "Authorization: Bearer <token>" et les informations de l'utilisateur connecte.
 */
public class LoginResponse {

    private String token;
    private UtilisateurResponse utilisateur;

    public LoginResponse() {
    }

    public LoginResponse(String token, UtilisateurResponse utilisateur) {
        this.token = token;
        this.utilisateur = utilisateur;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UtilisateurResponse getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurResponse utilisateur) {
        this.utilisateur = utilisateur;
    }
}
