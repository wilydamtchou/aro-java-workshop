window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // Configuration for DigiBank API  
  window.ui = SwaggerUIBundle({
    url: "/v3/api-docs",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    validatorUrl: ""
  });

  //</editor-fold>
};
