package tn.enicarthage.gestionRefugeAnimalier.dto;

/** Corps JSON recu par POST /auth/login : {"email": "...", "motDePasse": "..."} */
public class LoginRequest {

    private String email;
    private String motDePasse;

    public LoginRequest() {
    }

    public LoginRequest(String email, String motDePasse) {
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
