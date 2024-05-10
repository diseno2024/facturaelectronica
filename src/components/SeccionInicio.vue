<template>
    <section class="seccion">
      <div>
        <h5 class="titulo">{{ titulo }}</h5>
        <h2 class="contenido">{{ contenido }}</h2>
    </div>
    <img src="../assets/telefono.png" alt="Teléfono" class="imagen-telefono">
    </section>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const titulo = ref('BILLSV')
  const contenido = ref('Moderniza tu contabilidad con nuestra app de facturación electrónica.')
  </script>
  
  <style scoped src="../assets/styles.css"></style>
  