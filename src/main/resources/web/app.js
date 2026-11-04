document
    .querySelectorAll("form[data-confirm]")
    .forEach((formulario) => {

        formulario.addEventListener(
            "submit",
            (evento) => {

                const mensaje =
                    formulario.getAttribute(
                        "data-confirm"
                    );

                if (!window.confirm(mensaje)) {
                    evento.preventDefault();
                }
            }
        );
    });