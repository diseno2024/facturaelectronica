<template>
    
      <section class="benefits-section">
        <div class="container">
          <h2>Beneficios de BillSV</h2>
  
          <div class="benefits-grid">
            <div class="benefit-card benefit-card-enter">
              <h3>Respalda y restaura datos localmente</h3>
              <p>Control total y seguridad al respaldar y restaurar datos localmente.</p>
            </div>
  
            <div class="benefit-card benefit-card-enter">
              <h3>Genera facturas en minutos</h3>
              <p>Con nuestra aplicación, puedes generar facturas en cuestión de minutos.</p>
            </div>
  
            <div class="benefit-card benefit-card-enter">
              <h3>Visualiza ganancias, ingresos y gastos</h3>
              <p>Visualiza fácilmente ingresos, gastos y ganancias mensuales para tomar decisiones informadas.</p>
            </div>
          </div>
        </div>
      </section>
    
  </template>
  <script>
import IntersectionObserver from 'intersection-observer';

export default {
  mounted() {
    // Seleccionar la sección de beneficios
    const benefitsSection = document.querySelector('.benefits-section');

    // Crear una instancia de Intersection Observer
    const options = {
      root: null,
      threshold: 0.5 // La sección se considera visible cuando el 50% está en pantalla
    };

    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          // Aplicar la clase benefit-card-enter a cada tarjeta de beneficios
          document.querySelectorAll('.benefit-card').forEach(card => {
            card.classList.add('benefit-card-enter');
          });

          // Detener la observación una vez que se haya activado la animación
          observer.unobserve(benefitsSection);
        }
      });
    }, options);

    // Observar la sección de beneficios
    observer.observe(benefitsSection);

    // Reanudar la observación cuando la sección vuelva a salir de la vista
    window.addEventListener('scroll', () => {
      if (!observer.rootBounds) {
        observer.observe(benefitsSection);
      }
    });
  }
}

  </script>
  
  <style scoped>
.benefits-section {
  padding: 20px;
  background-color: #f0f0f0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 5px;
}

.benefit-card {
  background-color: #80BFA8;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.benefit-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}

.benefit-card p {
  font-size: 14px;
  line-height: 1.5;
}

.benefit-card-enter {
  animation: slide-in 1s ease-in-out forwards;
}

@keyframes slide-in {
  from {
    transform: translateX(-50%); /* Empieza fuera de la pantalla a la izquierda */
    opacity: 0; /* Comienza invisible */
  }
  to {
    transform: translateX(0); /* Se desliza a su posición final */
    opacity: 5; /* Se vuelve visible */
  }
}
  </style>