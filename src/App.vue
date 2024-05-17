<template>
  <div id="app">
    <NavBar />
    <div id="inicio">
      <SeccionInicio />
    </div>
    <div id="beneficios">
      <BeneficiosBill />
    </div>
    <div id="sobre-nosotros">
      <SobreNosotros/> 
    </div>
    <div id="quienes-somos">
      <QuienesSomos/> 
    </div>
    <div id="descarga-app">
      <DescargaApp/>
    </div>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import SeccionInicio from './components/SeccionInicio.vue'
import BeneficiosBill from './components/BeneficiosBill.vue'
import SobreNosotros from './components/SobreNosotros.vue'
import QuienesSomos from './components/QuienesSomos.vue'
import DescargaApp from './components/DescargaApp.vue'
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

section {
  padding: 20px 0;
}

#inicio {
  scroll-margin-top: 60px;
}

#beneficios {
  scroll-margin-top: 60px;
}

#sobre-nosotros {
  scroll-margin-top: 60px;
}
#quienes-somos {
  scroll-margin-top: 60px;
}

#descarga-app {
  scroll-margin-top: 60px;
}
</style>
