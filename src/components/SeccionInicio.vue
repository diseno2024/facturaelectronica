<template>
  <section class="seccion" ref="seccion">
    <div>
      <h5 class="titulo">{{ titulo }}</h5>
       
      <h2 class="contenido">{{ contenido }}</h2>
    </div>
    <img src="../assets/telefono.png" alt="Teléfono" class="imagen-telefono">
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const titulo = ref('BILLSV');
const contenido = ref('Moderniza tu contabilidad con nuestra app de facturación electrónica.');

const isElementVisible = (el) => {
  const rect = el.getBoundingClientRect();
  return (
    rect.top >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight)
  );
};

const handleScroll = () => {
  const section = document.querySelector('.seccion');
  if (section) {
    if (isElementVisible(section) || window.scrollY === 0) {
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

<style scoped src="../assets/styles.css">
/* Estilos de animación */
@keyframes desplazamiento {
  0% {
    transform: translateY(-20px); /* Empieza 20px arriba de su posición normal */
    opacity: 0; /* Empieza completamente transparente */
  }
  100% {
    transform: translateY(0); /* Vuelve a su posición normal */
    opacity: 1; /* Se vuelve completamente visible */
  }
}

/* Aplica la animación a la clase .animate */
.animate {
  animation: desplazamiento 1s ease forwards;
}

</style>