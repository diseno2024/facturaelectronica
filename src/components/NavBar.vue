<template>
  <nav class="navbar">
    <div class="logo">
      <span>BillSv</span>
    </div>
    <ul class="nav-links">
      <li><a @click.prevent="scrollToSection('inicio')">Inicio</a></li>
      <li><a @click.prevent="scrollToSection('beneficios')">Beneficios de BillSv</a></li>
      <li><a @click.prevent="scrollToSection('sobre-nosotros')">Sobre nosotros</a></li>
      <li><a @click.prevent="scrollToSection('quienes-somos')">Equipo desarrollador</a></li>
      <li><a @click.prevent="scrollToSection('descarga-app')">Descaga BillSv</a></li>
    </ul>
  </nav>
</template>

<script>
export default {
  name: "NavBar",
  methods: {
    scrollToSection(sectionId) {
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ behavior: "smooth" });
      }
    },
  },
};
</script>

<style scoped>
.navbar {
  width: 100vw;
  unicode-bidi: isolate;
  -webkit-text-size-adjust: 100%;
  box-sizing: border-box;
  border-style: solid;
  tab-size: 4;
  border-width: 0;
  display: flex;
  margin-left: auto;
  margin-right: auto;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  z-index: 1000;
}

.logo {
  display: flex;
  align-items: center;
}

.logo img {
  height: 24px;
  margin-right: 0.5rem;
}

.logo span {
  font-size: 1.2rem;
  text-align: left;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 40px;
  color: inherit;
  padding: 0px 0px 0.3em;
  margin-top: 0px;
  margin-bottom: 0px;
  color: #80bfa8;
  font-weight: bold;
}

.nav-links {
  display: flex;
  list-style: none;
}

.nav-links li {
  margin: 0 1rem;
}

.nav-links a {
  text-decoration: none;
  font-family: "Helveltica", sans-serif;
  padding: 0px 0px 0.75em;
  margin-top: 0px;
  margin-bottom: 0px;
  color: inherit;
  font-size: 18px;
  font-weight: 900;
  text-align: left;
  letter-spacing: -0.25px;
  line-height: 36px;
  cursor: pointer;
}

.nav-links a:hover {
  color: #80bfa8;
}
</style>