<template>
  <div>
    <seccion-inicio />
  </div>
  <div>
    <beneficios-bill />
  </div>
  <div>
    <quienes-somos /> 
  </div>
</template>

<script setup>
import SeccionInicio from './components/SeccionInicio.vue'
import BeneficiosBill from './components/BeneficiosBill.vue'
import QuienesSomos from './components/QuienesSomos.vue'
</script>

<script>
export default {
  components: {
    SeccionInicio,
    BeneficiosBill,
    QuienesSomos,
  }
}
</script>


<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
