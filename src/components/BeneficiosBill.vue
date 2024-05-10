<template>
  <section class="benefits-section">
    <div class="container">
      <h2>Beneficios de BillSV</h2>

      <div class="benefits-grid">
        <div class="benefit-card">
          <h3>Respalda y restaura datos localmente</h3>
          <p>Control total y seguridad al respaldar y restaurar datos localmente.</p>
        </div>

        <div class="benefit-card">
          <h3>Genera facturas en minutos</h3>
          <p>Con nuestra aplicación, puedes generar facturas en cuestión de minutos.</p>
        </div>

        <div class="benefit-card">
          <h3>Visualiza ganancias, ingresos y gastos</h3>
          <p>Visualiza fácilmente ingresos, gastos y ganancias mensuales para tomar decisiones informadas.</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';



const handleScroll = () => {
  const section = document.querySelector('.benefits-section');
  if (section) {
    const rect = section.getBoundingClientRect();
    if (rect.top < window.innerHeight - 100 && rect.bottom >= 0) {
      section.classList.add('animate');
    } else {
      section.classList.remove('animate');
    }
  }
};


onMounted(() => {
  handleScroll();
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
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

.animate .benefit-card {
  animation: slide-in 1s ease-in-out forwards;
}

@keyframes slide-in {
  from {
    transform: translateX(-50%); /* Empieza fuera de la pantalla a la izquierda */
    opacity: 0; /* Comienza invisible */
  }
  to {
    transform: translateX(0); /* Se desliza a su posición final */
    opacity: 1; /* Se vuelve visible */
  }
}
</style>
