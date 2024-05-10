<template>
  <div class="about-us">
    <div class="left-section">
      <section class="about-section animated">
        <h1>Sobre nosotros</h1>
        <p>BillSV es una aplicación nueva en el mercado de facturación electrónica diseñada para simplificar y agilizar el proceso de emisión y gestión de facturas para pequeñas y medianas empresas. Con una interfaz intuitiva y herramientas avanzadas, ayudamos a los negocios a cumplir con los requisitos legales y mejorar su eficiencia operativa.</p>
      </section>
      <section class="findid-section animated">
        <h2>Como encontrarnos</h2>
        <p>Nuestra aplicación de facturación electrónica ofrece múltiples opciones para encontrarnos y comenzar a utilizar nuestros servicios. Puedes descargar nuestra aplicación desde la tienda de aplicaciones de tu dispositivo móvil.</p>
      </section>
      <section class="members-section animated">
        <h2>Miembros</h2>
        <p>Nuestro equipo está formado por desarrolladores de software, diseñadores de experiencia de usuario y especialistas en cumplimiento normativo. Trabajamos en estrecha colaboración para ofrecer una plataforma robusta y segura que cumpla con los estándares más exigentes del mercado.</p>
      </section>
    </div>
    <div class="right-section">
      <section class="mission-section animated">
        <h2>Nuestra misión</h2>
        <p>En BillSV, nuestra misión es proporcionar a las empresas una solución integral y confiable para sus necesidades de facturación electrónica. Nos esforzamos por simplificar los procesos contables y administrativos, permitiendo a nuestros usuarios centrarse en lo que realmente importa: hacer crecer sus negocios. Con un enfoque en la innovación y la excelencia en el servicio, estamos comprometidos a ser el socio de confianza de cada empresa que elija nuestra plataforma.</p>
      </section>
      <section class="customer-service-section animated">
        <h2>Servicio al cliente</h2>
        <p>BillSV se compromete a brindar un servicio al cliente excepcional. Nuestro equipo de soporte técnico está disponible para resolver cualquier duda o problema que puedan tener nuestros usuarios. En nuestros contactos en linea personalizados para garantizar una experiencia fluida y sin contratiempos.</p>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutUs',
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
  },
  methods: {
    handleScroll() {
  const animatedElements = document.querySelectorAll('.animated');
  animatedElements.forEach(element => {
    if (this.isElementInViewport(element)) {
      if (!element.classList.contains('fadeIn')) {
        element.classList.add('fadeIn');
      }
    } else {
      if (element.classList.contains('fadeIn')) {
        element.classList.remove('fadeIn');
      }
    }
  });
},
    isElementInViewport(el) {
      const rect = el.getBoundingClientRect();
      return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
        rect.right <= (window.innerWidth || document.documentElement.clientWidth)
      );
    }
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.handleScroll);
  }
}
</script>

<style scoped>
.about-us {
  display: flex;
  flex-wrap: wrap;
  flex-flow: row;
  -webkit-box-align: stretch;
  align-items: stretch;
  margin-top: 0px;
  margin-bottom: 0px;
  margin-block-start: 1em;
  margin-block-end: 1em;
  margin-inline-start: 0px;
  margin-inline-end: 0px;
  unicode-bidi: isolate;
}

.left-section {
 -webkit-box-flex: 2;
 flex-grow: 2;
 min-width: 58%;
 margin-right: 32px;
 box-sizing: border-box;
 display: block;
 unicode-bidi: isolate;
}

.right-section {
  -webkit-box-flex: 1;
  flex-grow: 1;
  min-width: 375px;
  box-sizing: border-box;
  unicode-bidi: isolate;
}

.animated {
  opacity: 0;
}

.fadeIn {
  animation: fadeIn 1s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

h1 {
  text-align: left;
  font-size: 32px;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 40px;
  color: inherit;
  padding: 0px 0px 0.3em;
  margin-top: 0px;
  margin-bottom: 0px;
}

h2 {
  font-family: 'Helveltica', sans-serif;
  padding: 0px 0px 0.75em;
  margin-top: 0px;
  margin-bottom: 0px;
  color: inherit;
  font-size: 28px;
  font-weight: 900;
  text-align: left;
  letter-spacing: -0.25px;
  line-height: 36px;
}
p {
  font-family: 'Helveltica', sans-serif;
  padding: 0px 0px 1em;;
  margin-top: 0px;
  margin-bottom: 0px;
  color: inherit;
  font-size: 16px;
  font-weight: 400;
  text-align: justify;
  line-height: 24px;
  letter-spacing: 0px;
}
@media (max-width: 768px) {
  .about-us {
    flex-direction: column;
  }
  .left-section,
  .right-section {
    min-width: unset;
    margin-right: 0;
  }
}

</style>