package templates.bonus;

import lombok.Getter;

@Getter
public enum Sites {
    DEFAULT("default");

    private final String site;

    Sites(String site) {
        this.site = site;
    }

}
