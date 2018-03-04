<template>
    <v-app>
        <v-content>
            <v-container fluid full-height>
                <v-layout align-center justify-center>
                    <v-flex xs12 sm8 md4>
                        <v-card class="elevation-12">
                            <v-toolbar dark color="primary">
                                <v-toolbar-title>Login form</v-toolbar-title>
                            </v-toolbar>
                            <v-form method="POST">
                                <v-card-text>
                                    <v-text-field prepend-icon="person" name="username" label="Login"
                                                  type="text" v-model="username"></v-text-field>
                                    <v-text-field prepend-icon="lock" name="password" label="Password" id="password"
                                                  type="password"
                                                  v-model="password"></v-text-field>
                                </v-card-text>
                                <v-card-actions>
                                    <v-spacer></v-spacer>
                                    <v-btn color="primary" type="submit">Login</v-btn>
                                </v-card-actions>
                            </v-form>
                        </v-card>
                    </v-flex>
                </v-layout>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    export default {
        data() {
            return {
                username: null,
                password: null
            }
        }
    }
</script>

<style scoped>

</style>