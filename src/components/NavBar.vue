<template>
  <nav class="navbar">
    <div class="logo">
      <img src="../assets/Logo_Billsv.png" alt="Icono de BillSV">
    </div>
    <div class="menu-icon" @click="toggleMenu">
      <span class="material-symbols-outlined">menu</span>
    </div>
    <ul :class="{ 'nav-links': true, 'nav-links-mobile': showMenu }">
      <li><a @click.prevent="scrollToSection('inicio')">Inicio</a></li>
      <li><a @click.prevent="scrollToSection('beneficios')">Beneficios de BillSv</a></li>
      <li><a @click.prevent="scrollToSection('sobre-nosotros')">Sobre nosotros</a></li>
      <li><a @click.prevent="scrollToSection('quienes-somos')">Equipo desarrollador</a></li>
      <li><a @click.prevent="scrollToSection('descarga-app')">Descaga BillSv</a></li>
      <li><a @click="Verterminos">Términos de Uso</a></li>
    </ul>
  </nav>
</template>

<script>
export default {
  name: "NavBar",
  data() {
    return {
      showMenu: false
    };
  },
  methods: {
    toggleMenu() {
      this.showMenu = !this.showMenu;
    },
    scrollToSection(sectionId) {
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ behavior: "smooth" });
      }
    },
    Verterminos() {
      this.$router.push({ name: 'TerminosDeUso' });
    },
  },
};
</script>

<style scoped>
.navbar {
  width: 100vw;
  box-sizing: border-box;
  display: flex;
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
  width: 80px;
  height: 80px;
  margin-right: 20px;
  }

.menu-icon {
  display: none;
  cursor: pointer;
}

.menu-icon img {
  height: 24px;
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
  font-family: "Helvetica", sans-serif;
  color: inherit;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
}

.nav-links a:hover {
  color: #80bfa8;
}

.nav-links-mobile {
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .menu-icon {
    display: block;
  }

  .nav-links {
    display: none;
  }

  .nav-links.nav-links-mobile {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 60px;
    left: 0;
    width: 100%;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    z-index: 999;
  }

  .nav-links li {
    margin: 1rem 0;
  }
}
</style>
